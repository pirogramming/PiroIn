import '../../assets/styles/global.css';
import { useState, useEffect, useRef } from 'react';
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
    const [selectedImage, setSelectedImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const fileInputRef = useRef(null);

    const [commentMenuId, setCommentMenuId] = useState(null);
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editCommentText, setEditCommentText] = useState('');

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
        });
    };

    const handleCommentDelete = async (commentId) => {
        if (!window.confirm('댓글을 삭제할까요?')) return;
        try {
            const res = await authFetch(`/api/comments/${commentId}`, { method: 'DELETE' });
            if (!res.ok) throw new Error();
            setQuestion(prev => ({
                ...prev,
                comments: prev.comments.filter(c => c.commentId !== commentId),
            }));
        } catch (err) {
            console.error('댓글 삭제 실패:', err);
        }
        setCommentMenuId(null);
    };

    const handleCommentEditStart = (comment) => {
        setEditingCommentId(comment.commentId);
        setEditCommentText(comment.content);
        setCommentMenuId(null);
    };

    const handleCommentEditSubmit = async (commentId) => {
        const text = editCommentText.trim();
        if (!text) return;
        try {
            const res = await authFetch(`/api/comments/${commentId}`, {
                method: 'PATCH',
                body: JSON.stringify({ content: text }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setQuestion(prev => ({
                    ...prev,
                    comments: prev.comments.map(c =>
                        c.commentId === commentId ? { ...c, content: text } : c
                    ),
                }));
                setEditingCommentId(null);
            }
        } catch (err) {
            console.error('댓글 수정 실패:', err);
        }
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

                if (result.imageUrl) {
                    try {
                        const imgRes = await authFetch(result.imageUrl);
                        const blob = await imgRes.blob();
                        result.imageUrl = URL.createObjectURL(blob);
                    } catch {
                        result.imageUrl = null;
                    }
                }

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
                setQuestion(result);
            } catch (err) {
                console.error('질문 불러오기 실패:', err);
            } finally {
                setLoading(false);
            }
        };
        if (questionId) fetchQuestion();
    }, [questionId]);

    useEffect(() => {
        const handleClickOutside = () => {
            setShowMenu(false);
            setCommentMenuId(null);  // ← 추가
        };
        if (showMenu || commentMenuId) document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, [showMenu, commentMenuId]);

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

    const handleImageSelect = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setSelectedImage(file);
        setImagePreview(URL.createObjectURL(file));
    };

    const uploadImage = async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const token = localStorage.getItem('token');
        const res = await fetch('/api/images', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData,
        });
        const json = await res.json();
        return json.imageUrl;
    };

    const handlePaste = (e) => {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                const file = item.getAsFile();
                if (file) {
                    setSelectedImage(file);
                    setImagePreview(URL.createObjectURL(file));
                }
                break;
            }
        }
    };

    const handleCommentSubmit = async () => {
        const text = commentText.trim();
        if (!text) return;
        setIsSubmitting(true);
        try {
            let imageUrl = null;
            if (selectedImage) {
                imageUrl = await uploadImage(selectedImage);
            }
            const res = await authFetch(`/api/questions/${questionId}/comments`, {
                method: 'POST',
                body: JSON.stringify({ content: text, parentCommentId: null, imageUrl }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                if (isStaff) {
                    await authFetch(`/api/questions/${questionId}/status`, { method: 'PATCH' });
                    setQuestion(prev => ({ ...prev, isResolved: true }));
                } else if (question.isResolved) {
                    await authFetch(`/api/questions/${questionId}/status`, { method: 'PATCH' });
                    setQuestion(prev => ({ ...prev, isResolved: false }));
                }

                const newComment = {
                    commentId: json.result.commentId,
                    displayName: json.result.displayName,
                    content: json.result.content,
                    createdAt: json.result.createdAt,
                    imageUrl: imagePreview,
                };
                setQuestion(prev => ({
                    ...prev,
                    comments: [...(prev.comments ?? []), newComment],
                }));
                setCommentText('');
                setSelectedImage(null);
                setImagePreview(null);
            }
        } catch (err) {
            console.error('댓글 등록 실패:', err);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return <div className={styles.page}>불러오는 중...</div>;
    if (!question) return <div className={styles.page}>질문을 찾을 수 없어요</div>;

    const isMyQuestion = question.isMine;

    return (
        <div className={styles.page}>

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
                        <button className={styles.menuBtn} aria-label="더보기" onClick={(e) => { e.stopPropagation(); setShowMenu(prev => !prev); }}>
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

            {/* 상단 바: 해결 여부 */}
            <div className={styles.topBar}>
                {question.isResolved ? (
                    <span className={styles.solvedBadge}>해결 질문</span>
                ) : (
                    <span className={styles.unsolvedBadge}>미해결 질문</span>
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
                                {comment.displayName?.startsWith('운영진') && (
                                    <span className={styles.staffBadge}><StaffCheck /></span>
                                )}
                            </span>
                            {/* 본인 댓글이면 메뉴 버튼 표시 */}
                            {comment.displayName === '작성자' && (
                                <div style={{ position: 'relative', marginLeft: 'auto' }}>
                                    <button
                                        className={styles.menuBtn}
                                        onClick={(e) => { e.stopPropagation(); setCommentMenuId(prev => prev === comment.commentId ? null : comment.commentId); }}
                                    >
                                        <FiMoreVertical size={16} />
                                    </button>
                                    {commentMenuId === comment.commentId && (
                                        <div className={styles.dropdownMenu}>
                                            <button className={styles.dropdownItem} onClick={() => handleCommentEditStart(comment)}>수정</button>
                                            <button className={styles.dropdownItem} onClick={() => handleCommentDelete(comment.commentId)}>삭제</button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                        <div className={styles.commentBubble}>
                            {editingCommentId === comment.commentId ? (
                                <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
                                    <textarea
                                        className={styles.editInput}
                                        value={editCommentText}
                                        onChange={e => setEditCommentText(e.target.value)}
                                        autoFocus
                                    />
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                        <button className={styles.editConfirmBtn} onClick={() => handleCommentEditSubmit(comment.commentId)}>완료</button>
                                        <button className={styles.editCancelBtn} onClick={() => setEditingCommentId(null)}>취소</button>
                                    </div>
                                </div>
                            ) : (
                                <div className={styles.commentContent}>
                                    <FiCornerDownRight size={14} className={styles.commentArrow} />
                                    {comment.content}
                                </div>
                            )}
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
                {imagePreview && (
                    <div className={styles.imagePreviewWrapper}>
                        <img src={imagePreview} alt="미리보기" className={styles.imagePreview} />
                        <button
                            className={styles.imageRemoveBtn}
                            onClick={() => { setSelectedImage(null); setImagePreview(null); }}
                        >✕</button>
                    </div>
                )}
                <div className={styles.commentInputRow}>
                    <button
                        className={styles.commentPlusBtn}
                        onClick={() => fileInputRef.current?.click()}
                    >+</button>
                    <input
                        type="file"
                        accept="image/*"
                        ref={fileInputRef}
                        style={{ display: 'none' }}
                        onChange={handleImageSelect}
                    />
                    <input
                        id="commentInput"
                        className={styles.commentInput}
                        placeholder="댓글을 입력해주세요..."
                        value={commentText}
                        onChange={e => setCommentText(e.target.value)}
                        onKeyDown={e => { if (e.key === 'Enter') handleCommentSubmit(); }}
                        onPaste={handlePaste}
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
        </div>
    );
}

export default QnADetailPage;