import '../../assets/styles/global.css';
import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './QnADetailPage.module.css';
import { FiMoreVertical, FiCornerDownRight, FiChevronLeft } from 'react-icons/fi';
import {
    CommentImoji,
    MeCuriousToo,
    StaffCheck,
    SumitBtn,
    uploadImage,
} from '../../utils/qnaUtils';
import profileImg from '../../assets/images/profile.png';
import { authFetch } from '../../utils/Api';

// 시간만 표시하는 포맷 함수 (HH:MM)
const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    });
};

function QnADetailPage() {
    const { sessionId, questionId } = useParams();
    const navigate = useNavigate();
    const isStaff = localStorage.getItem('role') === 'ADMIN';

    // ── 질문 / 로딩 상태 ─────────────────────────────
    const [question, setQuestion] = useState(null);
    const [loading, setLoading] = useState(true);

    // ── 질문 수정 상태 ───────────────────────────────
    const [showMenu, setShowMenu] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [editText, setEditText] = useState('');

    // ── 댓글 입력 상태 ───────────────────────────────
    const [commentText, setCommentText] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [selectedImage, setSelectedImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const fileInputRef = useRef(null);

    // ── 댓글 수정 상태 ───────────────────────────────
    const [commentMenuId, setCommentMenuId] = useState(null);
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editCommentText, setEditCommentText] = useState('');

    // ── 질문 불러오기 ────────────────────────────────
    useEffect(() => {
        document.title = "Q&A | PIROIN";

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
                setQuestion(result);
            } catch (err) {
                console.error('질문 불러오기 실패:', err);
            } finally {
                setLoading(false);
            }
        };
        if (questionId) fetchQuestion();
    }, [questionId]);

    // ── 메뉴 외부 클릭 시 닫기 ──────────────────────
    useEffect(() => {
        const handleClickOutside = () => {
            setShowMenu(false);
            setCommentMenuId(null);
        };
        if (showMenu || commentMenuId) document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, [showMenu, commentMenuId]);

    // ── 목록으로 가기 ────────────────────────────────
    const handleBackToList = () => {
        navigate(`/sessions/${sessionId}/questions/`);
    };

    // ── 좋아요 토글 ──────────────────────────────────
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

    // ── 질문 수정 ────────────────────────────────────
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

    // ── 질문 삭제 ────────────────────────────────────
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

    // ── 질문 해결됨 처리 (운영진 전용) ──────────────
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

    // ── 댓글 이미지 선택 / 붙여넣기 ─────────────────
    const handleImageSelect = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setSelectedImage(file);
        setImagePreview(URL.createObjectURL(file));
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

    // ── 댓글 등록 ────────────────────────────────────
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
                // 댓글 등록 응답값으로 해결 상태 반영
                setQuestion(prev => ({ ...prev, isResolved: json.result.isResolved }));

                const newComment = {
                    commentId: json.result.commentId,
                    displayName: json.result.displayName,
                    content: json.result.content,
                    createdAt: json.result.createdAt,
                    imageUrl: imagePreview,
                    isMine: true,
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

    // ── 댓글 삭제 ────────────────────────────────────
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

    // ── 댓글 수정 ────────────────────────────────────
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

    if (loading) return <div className={styles.page}>불러오는 중...</div>;
    if (!question) return <div className={styles.page}>질문을 찾을 수 없어요</div>;

    const isMyQuestion = question.isMine;

    return (
        <div className={styles.page}>

            {/* ── 목록으로 가기 ── */}
            <button
                className={styles.backToListBtn}
                onClick={handleBackToList}
            >
                <FiChevronLeft size={18} />
                목록으로
            </button>

            {/* ── 작성자 행 ── */}
            <div className={styles.authorRow}>
                <div className={styles.avatar}>
                    <img src={profileImg} alt={question.displayName} className={styles.avatarImg} />
                </div>
                <div className={styles.authorInfo}>
                    <span className={styles.authorName}>익명</span>
                    <span className={styles.authorDate}>{formatTime(question.createdAt)}</span>
                </div>
                {(isMyQuestion || isStaff) && (
                    <div className={styles.menuWrapper}>
                        <button className={styles.menuBtn} aria-label="더보기"
                            onClick={(e) => { e.stopPropagation(); setShowMenu(prev => !prev); }}>
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

            {/* ── 해결 여부 뱃지 ── */}
            <div className={styles.topBar}>
                {question.isResolved ? (
                    <span className={styles.solvedBadge}>해결 질문</span>
                ) : (
                    <span className={styles.unsolvedBadge}>미해결 질문</span>
                )}
            </div>

            {/* ── 질문 본문 ── */}
            <div className={styles.questionTitle}>
                <span className={`${styles.qIcon} ${question.isResolved ? styles.qIconResolved : ''}`}>Q.</span>
                {isEditing ? (
                    <div className={styles.editWrapper}>
                        <textarea
                            className={styles.editInput}
                            value={editText}
                            onChange={e => setEditText(e.target.value)}
                            autoFocus
                        />
                        <div className={styles.editButtons}>
                            <button className={styles.editConfirmBtn} onClick={handleEditSubmit}>완료</button>
                            <button className={styles.editCancelBtn} onClick={() => setIsEditing(false)}>취소</button>
                        </div>
                    </div>
                ) : (
                    <span className={styles.questionText}>{question.content}</span>
                )}
            </div>

            {/* ── 질문 첨부 이미지 ── */}
            {question.imageUrl && (
                <img src={question.imageUrl} alt="첨부 이미지" className={styles.questionImage} />
            )}

            {/* ── 액션 버튼 (좋아요 / 댓글달기) ── */}
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

            {/* ── 댓글 목록 ── */}
            <div className={styles.commentList}>
                {question.comments?.map(comment => (
                    <div key={comment.commentId} className={styles.commentBlock}>

                        {/* 댓글 작성자 행 */}
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
                            {/* 본인 댓글만 수정/삭제 메뉴 표시 */}
                            {comment.isMine && (
                                <div className={styles.commentMenuWrapper}>
                                    <button
                                        className={styles.menuBtn}
                                        onClick={(e) => { e.stopPropagation(); setCommentMenuId(prev => prev === comment.commentId ? null : comment.commentId); }}
                                    >
                                        <FiMoreVertical size={16} />
                                    </button>
                                    {commentMenuId === comment.commentId && (
                                        <div className={styles.dropdownMenu}>
                                            <button className={styles.dropdownItem} onClick={(e) => { e.stopPropagation(); handleCommentEditStart(comment); }}>수정</button>
                                            <button className={styles.dropdownItem} onClick={(e) => { e.stopPropagation(); handleCommentDelete(comment.commentId); }}>삭제</button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* 댓글 말풍선 */}
                        <div className={styles.commentBubble}>
                            {editingCommentId === comment.commentId ? (
                                <div className={styles.commentEditWrapper}>
                                    <textarea
                                        className={styles.editCommentInput}
                                        value={editCommentText}
                                        onChange={e => setEditCommentText(e.target.value)}
                                        autoFocus
                                    />
                                    <div className={styles.commentEditButtons}>
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
                        <p className={styles.commentDate}>{formatTime(comment.createdAt)}</p>
                    </div>
                ))}
            </div>

            <div className={styles.bottomCover} />

            {/* ── 하단 댓글 입력바 ── */}
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