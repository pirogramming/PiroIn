import '../../assets/styles/global.css';
import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './QnADetailPage.module.css';
import { FiMoreVertical, FiCornerDownRight, FiChevronLeft, FiChevronRight, FiX } from 'react-icons/fi';
import {
    CommentImoji,
    MeCuriousToo,
    StaffCheck,
    SumitBtn,
    uploadImages,
} from '../../utils/qnaUtils';
import profileImg from '../../assets/images/profile.png';
import { authFetch } from '../../utils/Api';
import { subscribeQuestionEvents } from '../../utils/sse';

const POPULAR_LIKE_THRESHOLD = 5;

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

const createBlobImageUrl = async (imageUrl) => {
    if (!imageUrl) return null;

    try {
        const imgRes = await authFetch(imageUrl);
        const blob = await imgRes.blob();
        return URL.createObjectURL(blob);
    } catch {
        return null;
    }
};

// imageUrls 배열을 blob URL 배열로 변환
const createBlobImageUrls = async (imageUrls) => {
    if (!imageUrls || imageUrls.length === 0) return [];
    return Promise.all(imageUrls.map(url => createBlobImageUrl(url)));
};

const attachCommentBlobImages = async (comments = []) => Promise.all(
    comments.map(async (comment) => ({
        ...comment,
        imageUrls: await createBlobImageUrls(comment.imageUrls ?? []),
        replies: await attachCommentBlobImages(comment.replies ?? []),
    }))
);

const removeCommentFromTree = (comments = [], commentId) => comments
    .filter(comment => comment.commentId !== commentId)
    .map(comment => ({
        ...comment,
        replies: removeCommentFromTree(comment.replies ?? [], commentId),
    }));

const updateCommentInTree = (comments = [], commentId, updater) => comments.map(comment => {
    if (comment.commentId === commentId) {
        return updater(comment);
    }

    return {
        ...comment,
        replies: updateCommentInTree(comment.replies ?? [], commentId, updater),
    };
});

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
    const [selectedImages, setSelectedImages] = useState([]);       // 여러 장
    const [imagePreviews, setImagePreviews] = useState([]);         // 여러 장 미리보기
    const fileInputRef = useRef(null);

    // ── 댓글 수정 상태 ───────────────────────────────
    const [commentMenuId, setCommentMenuId] = useState(null);
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editCommentText, setEditCommentText] = useState('');

    // ── 이미지 확대보기(라이트박스) 상태 ─────────────
    // images: 같은 묶음(질문 또는 한 댓글)의 이미지 url 배열, index: 현재 보고 있는 인덱스
    const [lightbox, setLightbox] = useState(null);

    const openLightbox = (images, index) => setLightbox({ images, index });
    const closeLightbox = () => setLightbox(null);

    const showPrevImage = useCallback(() => {
        setLightbox(prev => {
            if (!prev) return prev;
            const nextIndex = (prev.index - 1 + prev.images.length) % prev.images.length;
            return { ...prev, index: nextIndex };
        });
    }, []);

    const showNextImage = useCallback(() => {
        setLightbox(prev => {
            if (!prev) return prev;
            const nextIndex = (prev.index + 1) % prev.images.length;
            return { ...prev, index: nextIndex };
        });
    }, []);

    useEffect(() => {
        if (!lightbox) return undefined;

        const handleKeyDown = (e) => {
            if (e.key === 'Escape') closeLightbox();
            if (e.key === 'ArrowLeft') showPrevImage();
            if (e.key === 'ArrowRight') showNextImage();
        };
        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [lightbox, showPrevImage, showNextImage]);

    // 모바일 좌우 스와이프로 이미지 넘기기
    const touchStartXRef = useRef(null);

    const handleLightboxTouchStart = (e) => {
        touchStartXRef.current = e.touches[0].clientX;
    };

    const handleLightboxTouchEnd = (e) => {
        if (touchStartXRef.current === null) return;
        const deltaX = e.changedTouches[0].clientX - touchStartXRef.current;
        const SWIPE_THRESHOLD = 50;
        if (deltaX > SWIPE_THRESHOLD) {
            showPrevImage();
        } else if (deltaX < -SWIPE_THRESHOLD) {
            showNextImage();
        }
        touchStartXRef.current = null;
    };


    const fetchQuestion = useCallback(async ({ showLoading = false } = {}) => {
        try {
            if (showLoading) {
                setLoading(true);
            }

            const res = await authFetch(`/api/questions/${questionId}`);
            if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
            const json = await res.json();
            if (!json.isSuccess) throw new Error(json.message);

            const result = json.result;

            // 질문 이미지 blob 변환 (여러 장)
            result.imageUrls = await createBlobImageUrls(result.imageUrls ?? []);

            // 댓글과 대댓글 이미지 blob 변환
            if (result.comments) {
                result.comments = await attachCommentBlobImages(result.comments);
            }
            setQuestion(result);
        } catch (err) {
            console.error('질문 불러오기 실패:', err);
        } finally {
            if (showLoading) {
                setLoading(false);
            }
        }
    }, [questionId]);

    // ── 질문 불러오기 ────────────────────────────────
    useEffect(() => {
        document.title = "Q&A | PIROIN";

        if (questionId) {
            void fetchQuestion({ showLoading: true });
        }
    }, [questionId, fetchQuestion]);

    const handleQuestionEvent = useCallback((message) => {
        if (String(message.data?.questionId) !== String(questionId)) {
            return;
        }

        if (message.event === 'comment-created' || message.event === 'comment-updated') {
            void fetchQuestion();
            return;
        }

        if (message.event === 'question-updated') {
            if (message.data?.isDeleted) {
                navigate(-1);
                return;
            }

            setQuestion(prev => {
                if (!prev) return prev;

                return {
                    ...prev,
                    content: message.data.content ?? prev.content,
                    isResolved: message.data.isResolved ?? prev.isResolved,
                    isPopular: message.data.isResolved === true
                        ? false
                        : (message.data.likeCount ?? prev.likeCount) >= POPULAR_LIKE_THRESHOLD,
                    likeCount: message.data.likeCount ?? prev.likeCount,
                };
            });
        }
    }, [fetchQuestion, navigate, questionId]);

    useEffect(() => {
        if (!sessionId || !questionId) {
            return undefined;
        }

        return subscribeQuestionEvents(sessionId, {
            onOpen: () => {
                console.debug('질문 상세 SSE 연결 열림');
            },
            onEvent: handleQuestionEvent,
            onError: (error) => {
                console.error('질문 상세 SSE 연결 실패:', error);
            },
        });
    }, [sessionId, questionId, handleQuestionEvent]);

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
        const files = Array.from(e.target.files);
        if (!files.length) return;
        // 최대 5장 제한
        const merged = [...selectedImages, ...files].slice(0, 5);
        setSelectedImages(merged);
        setImagePreviews(merged.map(f => URL.createObjectURL(f)));
        // 같은 파일 재선택 허용
        e.target.value = '';
    };

    const handlePaste = (e) => {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                const file = item.getAsFile();
                if (file) {
                    const merged = [...selectedImages, file].slice(0, 5);
                    setSelectedImages(merged);
                    setImagePreviews(merged.map(f => URL.createObjectURL(f)));
                }
                break;
            }
        }
    };

    const handleRemoveImage = (idx) => {
        const next = selectedImages.filter((_, i) => i !== idx);
        setSelectedImages(next);
        setImagePreviews(next.map(f => URL.createObjectURL(f)));
    };

    // ── 댓글 등록 ────────────────────────────────────
    const handleCommentSubmit = async () => {
        const text = commentText.trim();
        if (!text && selectedImages.length === 0) return;
        setIsSubmitting(true);
        try {
            let imageUrls = [];
            if (selectedImages.length > 0) {
                imageUrls = await uploadImages(selectedImages);
            }
            const res = await authFetch(`/api/questions/${questionId}/comments`, {
                method: 'POST',
                body: JSON.stringify({ content: text, parentCommentId: null, imageUrls }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setCommentText('');
                setSelectedImages([]);
                setImagePreviews([]);
                // 로컬 상태에 blob URL을 직접 넣으면 새로고침 시 이미지가 깨지므로
                // 등록 직후 fetchQuestion으로 서버의 정식 URL을 받아온다.
                await fetchQuestion();
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
                comments: removeCommentFromTree(prev.comments ?? [], commentId),
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
                    comments: updateCommentInTree(
                        prev.comments ?? [],
                        commentId,
                        comment => ({ ...comment, content: text })
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
    const renderCommentBlock = (comment, isReply = false) => (
        <div
            key={comment.commentId}
            className={`${styles.commentBlock} ${isReply ? styles.replyBlock : ''}`}
        >
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
                {comment.imageUrls?.length > 0 && (
                    <div className={styles.commentImages}>
                        {comment.imageUrls.map((url, idx) => (
                            <img
                                key={idx}
                                src={url}
                                alt={`댓글 첨부 이미지 ${idx + 1}`}
                                className={styles.commentImage}
                                onClick={() => openLightbox(comment.imageUrls, idx)}
                            />
                        ))}
                    </div>
                )}
            </div>
            <p className={styles.commentDate}>{formatTime(comment.createdAt)}</p>

            {comment.replies?.length > 0 && (
                <div className={styles.replyList}>
                    {comment.replies.map(reply => renderCommentBlock(reply, true))}
                </div>
            )}
        </div>
    );

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

            {/* ── 질문 첨부 이미지 (여러 장) ── */}
            {question.imageUrls?.length > 0 && (
                <div className={styles.questionImages}>
                    {question.imageUrls.map((url, idx) => (
                        <img
                            key={idx}
                            src={url}
                            alt={`첨부 이미지 ${idx + 1}`}
                            className={styles.questionImage}
                            onClick={() => openLightbox(question.imageUrls, idx)}
                        />
                    ))}
                </div>
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
                {question.comments?.map(comment => renderCommentBlock(comment))}
            </div>

            <div className={styles.bottomCover} />

            {/* ── 하단 댓글 입력바 ── */}
            <div className={styles.commentInputBar}>
                {imagePreviews.length > 0 && (
                    <div className={styles.imagePreviewList}>
                        {imagePreviews.map((preview, idx) => (
                            <div key={idx} className={styles.imagePreviewWrapper}>
                                <img src={preview} alt={`미리보기 ${idx + 1}`} className={styles.imagePreview} />
                                <button
                                    className={styles.imageRemoveBtn}
                                    onClick={() => handleRemoveImage(idx)}
                                >✕</button>
                            </div>
                        ))}
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
                        multiple
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
                        disabled={(!commentText.trim() && selectedImages.length === 0) || isSubmitting}
                        aria-label="댓글 제출"
                    >
                        {isSubmitting ? '⏳' : <SumitBtn />}
                    </button>
                </div>
            </div>

            {/* ── 이미지 확대보기 ── */}
            {lightbox && (
                <div
                    className={styles.lightboxOverlay}
                    onClick={closeLightbox}
                    onTouchStart={handleLightboxTouchStart}
                    onTouchEnd={handleLightboxTouchEnd}
                >
                    <button
                        className={styles.lightboxCloseBtn}
                        onClick={closeLightbox}
                        aria-label="닫기"
                    >
                        <FiX size={28} />
                    </button>

                    {lightbox.images.length > 1 && (
                        <button
                            className={styles.lightboxPrevBtn}
                            onClick={(e) => { e.stopPropagation(); showPrevImage(); }}
                            aria-label="이전 이미지"
                        >
                            <FiChevronLeft size={28} />
                        </button>
                    )}

                    <img
                        src={lightbox.images[lightbox.index]}
                        alt={`확대 이미지 ${lightbox.index + 1}`}
                        className={styles.lightboxImage}
                        onClick={(e) => e.stopPropagation()}
                    />

                    {lightbox.images.length > 1 && (
                        <button
                            className={styles.lightboxNextBtn}
                            onClick={(e) => { e.stopPropagation(); showNextImage(); }}
                            aria-label="다음 이미지"
                        >
                            <FiChevronRight size={28} />
                        </button>
                    )}

                    {lightbox.images.length > 1 && (
                        <div className={styles.lightboxCounter}>
                            {lightbox.index + 1} / {lightbox.images.length}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default QnADetailPage;