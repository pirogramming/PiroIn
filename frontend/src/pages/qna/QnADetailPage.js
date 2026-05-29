import '../../assets/styles/global.css';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './QnADetailPage.module.css';
import { FiMoreVertical, FiCornerDownRight } from 'react-icons/fi';
import {
    CommentImoji,
    MeCuriousToo,
    StaffCheck,
    SumitBtn,
} from '../../components/qna_svg';
import profileImg from '../../assets/images/profile.png';
import { authFetch } from '../../utils/Api';

function QnADetailPage() {
    const { questionId } = useParams();
    const navigate = useNavigate();
    const isStaff = localStorage.getItem('role') === 'ADMIN';

    const [question, setQuestion] = useState(null);
    const [commentText, setCommentText] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [loading, setLoading] = useState(true);
    const [showMenu, setShowMenu] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [editText, setEditText] = useState('');

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
        });
    };

    useEffect(() => {
        const fetchQuestion = async () => {
            try {
                setLoading(true);
                const res = await authFetch(`/api/questions/${questionId}`);
                if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
                const json = await res.json();
                if (!json.isSuccess) throw new Error(json.message);

                const result = json.result;

                // 질문 이미지 blob 변환
                if (result.imageUrl) {
                    try {
                        const imgRes = await authFetch(result.imageUrl);
                        const blob = await imgRes.blob();
                        result.imageUrl = URL.createObjectURL(blob);
                    } catch {
                        result.imageUrl = null;
                    }
                }

                // 댓글 이미지 blob 변환
                if (result.comments) {
                    result.comments = await Promise.all(
                        result.comments.map(async (comment) => {
                            if (comment.imageUrl) {
                                try {
                                    const imgRes = await authFetch(comment.imageUrl);
                                    const blob = await imgRes.blob();
                                    return { ...comment, imageUrl: URL.createObjectURL(blob) };
                                } catch {
                                    return { ...comment, imageUrl: null };
                                }
                            }
                            return comment;
                        })
                    );
                }

                console.log(result.displayName);
                setQuestion(result);
            } catch (err) {
                console.error('질문 불러오기 실패:', err);
            } finally {
                setLoading(false);
            }
        };
        if (questionId) fetchQuestion();
    }, [questionId]);

    const toggleLike = async () => {
        try {
            const res = await authFetch(`/api/questions/${questionId}/like`, { method: 'POST' });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setQuestion(prev => ({
                    ...prev,
                    likeCount: json.result.likeCount,
                    isLiked: json.result.isLiked,
                }));
            }
        } catch (err) {
            console.error('좋아요 실패:', err);
        }
    };

    const handleEditStart = () => {
        setEditText(question.content);
        setIsEditing(true);
        setShowMenu(false);
    };

    const handleEditSubmit = async () => {
        const text = editText.trim();
        if (!text) return;
        try {
            const res = await authFetch(`/api/questions/${questionId}/modify`, {
                method: 'PATCH',
                body: JSON.stringify({ content: text }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setQuestion(prev => ({ ...prev, content: text }));
                setIsEditing(false);
            }
        } catch (err) {
            console.error('수정 실패:', err);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm('질문을 삭제할까요?')) return;
        try {
            const res = await authFetch(`/api/questions/${questionId}`, { method: 'DELETE' });
            if (!res.ok) throw new Error();
            navigate(-1);
        } catch (err) {
            console.error('삭제 실패:', err);
        }
        setShowMenu(false);
    };

    const handleResolve = async () => {
        try {
            const res = await authFetch(`/api/questions/${questionId}/status`, { method: 'PATCH' });
            if (!res.ok) throw new Error();
            setQuestion(prev => ({ ...prev, isResolved: true }));
        } catch (err) {
            console.error('해결됨 처리 실패:', err);
        }
        setShowMenu(false);
    };

    const handleCommentSubmit = async () => {
        const text = commentText.trim();
        if (!text) return;
        setIsSubmitting(true);
        try {
            const res = await authFetch(`/api/questions/${questionId}/comments`, {
                method: 'POST',
                body: JSON.stringify({ content: text, parentCommentId: null }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                if (isStaff) {
                    await authFetch(`/api/questions/${questionId}/status`, { method: 'PATCH' });
                    setQuestion(prev => ({ ...prev, isResolved: true }));
                }
                const newComment = {
                    commentId: json.result.commentId,
                    displayName: json.result.displayName,
                    content: json.result.content,
                    createdAt: json.result.createdAt,
                    imageUrl: null,
                };
                setQuestion(prev => ({
                    ...prev,
                    comments: [...(prev.comments ?? []), newComment],
                }));
                setCommentText('');
            }
        } catch (err) {
            console.error('댓글 등록 실패:', err);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return <div className={styles.page}>불러오는 중...</div>;
    if (!question) return <div className={styles.page}>질문을 찾을 수 없어요</div>;

    const isMyQuestion = question.displayName === '작성자';

    return (
        <div className={styles.page}>
            {/* 상단 바: 해결 여부 */}
            <div className={styles.topBar}>
                {question.isResolved ? (
                    <span className={styles.solvedBadge}>해결 질문</span>
                ) : (
                    <span className={styles.unsolvedBadge}>미해결 질문</span>
                )}
            </div>

            {/* 작성자 행 */}
            <div className={styles.authorRow}>
                <div className={styles.avatar}>
                    <img src={profileImg} alt={question.displayName} className={styles.avatarImg} />
                </div>
                <div className={styles.authorInfo}>
                    <span className={styles.authorName}>익명</span>
                    <span className={styles.authorDate}>{formatDate(question.createdAt)}</span>
                </div>
                {(isMyQuestion || isStaff) && (
                    <div style={{ position: 'relative' }}>
                        <button className={styles.menuBtn} aria-label="더보기" onClick={() => setShowMenu(prev => !prev)}>
                            <FiMoreVertical size={20} />
                        </button>
                        {showMenu && (
                            <div className={styles.dropdownMenu}>
                                {isMyQuestion && (
                                    <>
                                        <button className={styles.dropdownItem} onClick={handleEditStart}>수정</button>
                                        <button className={styles.dropdownItem} onClick={handleDelete}>삭제</button>
                                    </>
                                )}
                                {isStaff && !isMyQuestion && (
                                    <>
                                        <button className={styles.dropdownItem} onClick={handleDelete}>삭제</button>
                                        {!question.isResolved && (
                                            <button className={styles.dropdownItem} onClick={handleResolve}>해결됨으로</button>
                                        )}
                                    </>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>



            {/* 질문 내용 */}
            <div className={styles.questionTitle}>
                <span
                    className={styles.qIcon}
                    style={{ color: question.isResolved ? 'var(--gray600)' : 'var(--main)' }}
                >Q.</span>
                {isEditing ? (
                    <div style={{ flex: 1, display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
                        <textarea
                            className={styles.editInput}
                            value={editText}
                            onChange={e => setEditText(e.target.value)}
                            autoFocus
                        />
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                            <button className={styles.editConfirmBtn} onClick={handleEditSubmit}>완료</button>
                            <button className={styles.editCancelBtn} onClick={() => setIsEditing(false)}>취소</button>
                        </div>
                    </div>
                ) : (
                    <span className={styles.questionText}>{question.content}</span>
                )}
            </div>

            {/* 첨부 이미지 */}
            {question.imageUrl && (
                <img src={question.imageUrl} alt="첨부 이미지" className={styles.questionImage} />
            )}

            {/* 액션 버튼 */}
            <div className={styles.actionRow}>
                <button
                    className={`${styles.likeBtn} ${question.isLiked ? styles.liked : ''}`}
                    onClick={toggleLike}
                >
                    <MeCuriousToo /> 저도 궁금해요&nbsp;{question.likeCount}
                </button>
                <button
                    className={styles.commentBtn}
                    onClick={() => document.getElementById('commentInput')?.focus()}
                >
                    <CommentImoji />&nbsp;댓글달기
                </button>
            </div>

            <hr className={styles.divider} />

            {/* 댓글 목록 */}
            <div className={styles.commentList}>
                {question.comments?.map(comment => (
                    <div key={comment.commentId} className={styles.commentBlock}>
                        <div className={styles.commentAuthorRow}>
                            <div className={styles.commentAvatar}>
                                <img src={profileImg} alt={comment.displayName} className={styles.commentAvatarImg} />
                            </div>
                            <span className={styles.commentAuthorName}>
                                {comment.displayName}
                            </span>
                        </div>
                        <div className={styles.commentBubble}>
                            <div className={styles.commentContent}>
                                <FiCornerDownRight size={14} className={styles.commentArrow} />
                                {comment.content}
                            </div>
                            {comment.imageUrl && (
                                <img src={comment.imageUrl} alt="댓글 첨부 이미지" className={styles.commentImage} />
                            )}
                        </div>
                        <p className={styles.commentDate}>{formatDate(comment.createdAt)}</p>
                    </div>
                ))}
            </div>

            <div className={styles.bottomCover} />

            {/* 댓글 입력 바 */}
            <div className={styles.commentInputBar}>
                <input
                    id="commentInput"
                    className={styles.commentInput}
                    placeholder="댓글을 입력해주세요..."
                    value={commentText}
                    onChange={e => setCommentText(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter') handleCommentSubmit(); }}
                    disabled={isSubmitting}
                />
                <button
                    className={styles.submitBtn}
                    onClick={handleCommentSubmit}
                    disabled={!commentText.trim() || isSubmitting}
                    aria-label="댓글 제출"
                >
                    {isSubmitting ? '⏳' : <SumitBtn />}
                </button>
            </div>
        </div>
    );
}

export default QnADetailPage;