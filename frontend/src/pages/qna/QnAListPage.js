import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import styles from './QnAListPage.module.css';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import { authFetch } from '../../utils/Api';
import {
    CommentImoji, MeCuriousToo, SortBtn,
    OBtn, XBtn, CommentCommentArraw, SumitBtn, StaffCheck, ImgPreview,
} from '../../components/qna_svg';

const MAX_VISIBLE_COMMENTS = 3;

const DAY_PART_KO = { AM: '오전', PM: '오후' };
const DAY_OF_WEEK_KO = {
    MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수',
    THURSDAY: '목', FRIDAY: '금', SATURDAY: '토', SUNDAY: '일',
};

function QnAListPage() {
    const { sessionId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const isPast = location.state?.status === 'AFTER_SESSION';
    const isStaff = localStorage.getItem('role') === 'ADMIN';

    const [sessionTitle, setSessionTitle] = useState('');
    const [understanding, setUnderstanding] = useState(null);
    const [understandingIndex, setUnderstandingIndex] = useState(0);
    const [myChoices, setMyChoices] = useState({});

    const [popularQuestions, setPopularQuestions] = useState([]);
    const [unresolvedQuestions, setUnresolvedQuestions] = useState([]);
    const [resolvedQuestions, setResolvedQuestions] = useState([]);

    const [filterCurious, setFilterCurious] = useState(false);
    const [filterUnsolved, setFilterUnsolved] = useState(false);
    const [sortOrder, setSortOrder] = useState('정렬');
    const [showSortMenu, setShowSortMenu] = useState(false);

    const [commentOpenId, setCommentOpenId] = useState(null);
    const [commentInputs, setCommentInputs] = useState({});
    const [newQuestion, setNewQuestion] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [selectedImage, setSelectedImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [commentImages, setCommentImages] = useState({});
    const [commentImagePreviews, setCommentImagePreviews] = useState({});
    const commentFileRefs = useRef({});

    const fileInputRef = useRef(null);


    const fetchQuestions = useCallback(async (index) => {
        try {
            const res = await authFetch(`/api/sessions/${sessionId}/questions?understandingIndex=${index}`);
            if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
            const json = await res.json();
            if (!json.isSuccess) throw new Error(json.message);

            const { session, understanding, questions } = json.result;

            setSessionTitle(`${session.week}주차 ${DAY_OF_WEEK_KO[session.dayOfWeek]}요일 ${DAY_PART_KO[session.dayPart]} (${session.title})`);
            setUnderstanding(understanding);

            const allQ = [
                ...(questions.popularQuestions ?? []),
                ...(questions.unresolvedQuestions ?? []),
                ...(questions.resolvedQuestions ?? []),
            ];

            // 이미지 blob URL 변환만 처리 (개별 API 호출 제거)
            const withBlob = await Promise.all(
                allQ.map(async (q) => {
                    let blobImageUrl = null;
                    if (q.imageUrl) {
                        try {
                            const imgRes = await authFetch(q.imageUrl);
                            const blob = await imgRes.blob();
                            blobImageUrl = URL.createObjectURL(blob);
                        } catch {
                            blobImageUrl = null;
                        }
                    }
                    return { ...q, iLiked: q.isLiked, imageUrl: blobImageUrl };
                })
            );

            const idSet = (list) => new Set(list.map(q => q.questionId));
            const popularIds = idSet(questions.popularQuestions ?? []);
            const unresolvedIds = idSet(questions.unresolvedQuestions ?? []);
            const resolvedIds = idSet(questions.resolvedQuestions ?? []);

            setPopularQuestions(withBlob.filter(q => popularIds.has(q.questionId)));
            setUnresolvedQuestions(withBlob.filter(q => unresolvedIds.has(q.questionId)));
            setResolvedQuestions(withBlob.filter(q => resolvedIds.has(q.questionId)));

        } catch (err) {
            console.error('질문 불러오기 실패:', err);
        }
    }, [sessionId]);

    useEffect(() => {
        if (sessionId) fetchQuestions(understandingIndex);
    }, [sessionId, understandingIndex, fetchQuestions]);

    const goPrevUnderstand = () => {
        if (understanding?.hasOlder) setUnderstandingIndex(prev => prev + 1);
    };
    const goNextUnderstand = () => {
        if (understanding?.hasNewer) setUnderstandingIndex(prev => prev - 1);
    };

    const handleUnderstandChoice = async (choice) => {
        if (!understanding?.current?.checkId) return;
        const checkId = understanding.current.checkId;
        const newChoice = myChoices[checkId] === choice ? null : choice;
        setMyChoices(prev => ({ ...prev, [checkId]: newChoice }));
        if (!newChoice) return;
        try {
            const res = await authFetch(
                `/api/sessions/${sessionId}/understanding-checks/${checkId}/responses`,
                { method: 'POST', body: JSON.stringify({ choice: newChoice }) }
            );
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setUnderstanding(prev => ({
                    ...prev,
                    current: {
                        ...prev.current,
                        understoodCount: json.result.understoodCount,
                        notUnderstoodCount: json.result.notUnderstoodCount,
                        attendanceCount: json.result.attendanceCount,
                    }
                }));
            }
        } catch (err) {
            console.error('이해도 응답 실패:', err);
        }
    };

    const toggleLike = async (e, questionId) => {
        e.stopPropagation();
        if (isPast) return;
        try {
            const res = await authFetch(`/api/questions/${questionId}/like`, { method: 'POST' });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                const update = (list) => list.map(q =>
                    q.questionId === questionId
                        ? { ...q, likeCount: json.result.likeCount, iLiked: json.result.isLiked }
                        : q
                );
                setPopularQuestions(update);
                setUnresolvedQuestions(update);
                setResolvedQuestions(update);
            }
        } catch (err) {
            console.error('좋아요 실패:', err);
        }
    };

    const toggleCommentInput = (e, questionId) => {
        e.stopPropagation();
        if (isPast) return;
        setCommentOpenId(prev => prev === questionId ? null : questionId);
    };

    const handleCommentChange = (questionId, value) => {
        setCommentInputs(prev => ({ ...prev, [questionId]: value }));
    };

    const handleCommentSubmit = async (e, questionId) => {
        e.stopPropagation();
        const text = (commentInputs[questionId] || '').trim();
        if (!text) return;
        try {
            let imageUrl = null;
            if (commentImages[questionId]) {
                imageUrl = await uploadImage(commentImages[questionId]);
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
                }
                const newComment = {
                    commentId: json.result.commentId,
                    displayName: json.result.displayName,
                    content: json.result.content,
                };
                const update = (list) => list.map(q =>
                    q.questionId === questionId
                        ? {
                            ...q,
                            isResolved: isStaff ? true : q.isResolved,
                            previewComments: [...(q.previewComments ?? []), newComment],
                            commentCount: (q.commentCount ?? 0) + 1
                        }
                        : q
                );
                setPopularQuestions(update);
                setUnresolvedQuestions(update);
                setResolvedQuestions(update);
                setCommentInputs(prev => ({ ...prev, [questionId]: '' }));
                setCommentImages(prev => ({ ...prev, [questionId]: null }));
                setCommentImagePreviews(prev => ({ ...prev, [questionId]: null }));
                setCommentOpenId(null);
            }
        } catch (err) {
            console.error('댓글 등록 실패:', err);
        }
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
    const handleCommentImageSelect = (e, questionId) => {
        const file = e.target.files[0];
        if (!file) return;
        setCommentImages(prev => ({ ...prev, [questionId]: file }));
        setCommentImagePreviews(prev => ({ ...prev, [questionId]: URL.createObjectURL(file) }));
    };

    const handleCommentPaste = (e, questionId) => {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                const file = item.getAsFile();
                if (file) {
                    setCommentImages(prev => ({ ...prev, [questionId]: file }));
                    setCommentImagePreviews(prev => ({ ...prev, [questionId]: URL.createObjectURL(file) }));
                }
                break;
            }
        }
    };
    const handleNewQuestion = async () => {
        const text = newQuestion.trim();
        if (!text) return;
        setIsSubmitting(true);
        setSubmitError(null);
        try {
            let imageUrl = null;
            if (selectedImage) {
                imageUrl = await uploadImage(selectedImage);
            }
            const res = await authFetch(`/api/sessions/${sessionId}/questions`, {
                method: 'POST',
                body: JSON.stringify({ content: text, imageUrl }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setNewQuestion('');
                setSelectedImage(null);
                setImagePreview(null);
                fetchQuestions(understandingIndex);
            }
        } catch (err) {
            console.error('질문 등록 실패:', err);
            setSubmitError('질문 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleNewUnderstandCheck = async () => {
        const text = newQuestion.trim();
        if (!text) return;
        setIsSubmitting(true);
        setSubmitError(null);
        try {
            const res = await authFetch(`/api/sessions/${sessionId}/understanding-checks`, {
                method: 'POST',
                body: JSON.stringify({ content: text }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setNewQuestion('');
                setUnderstandingIndex(0);
                fetchQuestions(0);
            }
        } catch (err) {
            console.error('이해도 등록 실패:', err);
            setSubmitError('이해도 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const allQuestions = [
        ...popularQuestions,
        ...unresolvedQuestions.filter(q => !popularQuestions.some(p => p.questionId === q.questionId)),
        ...resolvedQuestions.filter(q => !popularQuestions.some(p => p.questionId === q.questionId)),
    ];

    const displayedQuestions = (() => {
        let list = allQuestions;
        if (isStaff && filterUnsolved) list = unresolvedQuestions;
        if (!isStaff && filterCurious) list = allQuestions.filter(q => q.iLiked);

        if (sortOrder === '최신순') {
            list = [...list].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else if (sortOrder === '저도궁금해요순') {
            list = [...list].sort((a, b) => b.likeCount - a.likeCount);
        }

        return list;
    })();

    const currentChoice = myChoices[understanding?.current?.checkId];

    return (
        <div className={styles.page}>
            <h1 className={styles.title}>{sessionTitle}</h1>

            <div className={styles.filterRow}>
                {isStaff ? (
                    <label className={styles.curiousLabel}>
                        <input type="checkbox" checked={filterUnsolved}
                            onChange={e => setFilterUnsolved(e.target.checked)}
                            className={styles.curiousCheckbox} />
                        미해결 질문
                    </label>
                ) : (
                    <label className={styles.curiousLabel}>
                        <input type="checkbox" checked={filterCurious}
                            onChange={e => setFilterCurious(e.target.checked)}
                            className={styles.curiousCheckbox} />
                        저도 궁금해요
                    </label>
                )}
                <div className={styles.sortWrapper}>
                    <button className={styles.sortBtn} onClick={() => setShowSortMenu(prev => !prev)}>
                        {sortOrder} <SortBtn />
                    </button>
                    {showSortMenu && (
                        <ul className={styles.sortMenu}>
                            {['기본', '최신순', '저도궁금해요순'].map(option => (
                                <li key={option} className={styles.sortOption}
                                    onClick={() => { setSortOrder(option); setShowSortMenu(false); }}>
                                    {option}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
            <hr className={styles.divider} />

            {/* 이해도 */}
            <div className={styles.understandBar}>
                <button className={styles.arrowBtn} onClick={goPrevUnderstand}
                    disabled={!understanding?.hasOlder}>
                    <FiChevronLeft size={30} />
                </button>
                <span className={styles.understandName}>
                    {understanding?.current?.content ?? '이해도 없음'}
                    <span className={styles.understandCount}>
                        ({understanding?.current?.respondedCount ?? 0}/
                        {understanding?.current?.attendanceCount ?? 0})
                    </span>
                </span>
                <button
                    className={`${styles.oxBtn} ${styles.oxO} ${currentChoice === 'UNDERSTOOD' ? styles.oxActive : ''}`}
                    onClick={() => handleUnderstandChoice('UNDERSTOOD')}
                    disabled={isStaff || isPast}  // ← isPast 추가
                >
                    <OBtn />
                    {isStaff && <span className={styles.oxCount}>{understanding?.current?.understoodCount ?? 0}</span>}
                </button>
                <button
                    className={`${styles.oxBtn} ${styles.oxX} ${currentChoice === 'NOT_UNDERSTOOD' ? styles.oxActive : ''}`}
                    onClick={() => handleUnderstandChoice('NOT_UNDERSTOOD')}
                    disabled={isStaff || isPast}  // ← isPast 추가
                >
                    <XBtn />
                    {isStaff && <span className={styles.oxCount}>{understanding?.current?.notUnderstoodCount ?? 0}</span>}
                </button>
                <button className={styles.arrowBtn} onClick={goNextUnderstand}
                    disabled={!understanding?.hasNewer}>
                    <FiChevronRight size={30} />
                </button>
            </div>

            {/* 질문 목록 */}
            <div className={styles.questionList}>
                {displayedQuestions.map(question => (
                    <div key={question.questionId} className={styles.questionCard}
                        onClick={() => navigate(`/sessions/${sessionId}/questions/${question.questionId}`)}>
                        <div className={styles.questionHeader}>
                            <span
                                className={styles.qIcon}
                                style={{ color: question.isResolved ? 'var(--gray600)' : '' }}
                            >Q.</span>
                            <span className={styles.questionText}>{question.content}</span>
                            <div className={styles.questionActions}>
                                <button
                                    className={`${styles.likeBtn} ${question.iLiked ? styles.liked : ''}`}
                                    onClick={e => toggleLike(e, question.questionId)}
                                >
                                    <MeCuriousToo />{question.likeCount}
                                </button>
                                {!isPast && (
                                    <button className={styles.commentBtn}
                                        onClick={e => toggleCommentInput(e, question.questionId)}>
                                        <CommentImoji />&nbsp;댓글달기
                                    </button>
                                )}
                            </div>
                        </div>

                        {question.imageUrl && (
                            <img src={question.imageUrl} alt="첨부 이미지"
                                className={styles.questionImage}
                                onClick={e => e.stopPropagation()} />
                        )}

                        {question.previewComments?.length > 0 && (
                            <div className={styles.commentPreview}>
                                {question.previewComments.slice(0, MAX_VISIBLE_COMMENTS).map(comment => (
                                    <div key={comment.commentId} className={styles.commentWrapper}>
                                        <span className={styles.commentAuthorName}>
                                            {comment.displayName}
                                            {comment.displayName?.startsWith('운영진') && (
                                                <span className={styles.staffBadge}><StaffCheck /></span>
                                            )}
                                        </span>
                                        <div className={styles.commentItem}>
                                            <div className={styles.commentContent}>
                                                <CommentCommentArraw /> {comment.content}
                                            </div>
                                            {comment.hasImage && (
                                                <div
                                                    className={styles.commentImagePreview}
                                                    onClick={e => {
                                                        e.stopPropagation();
                                                        navigate(`/sessions/${sessionId}/questions/${question.questionId}`);
                                                    }}
                                                    style={{ cursor: 'pointer' }}
                                                >
                                                    <span><ImgPreview /> 사진 보기</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}
                                {question.commentCount > MAX_VISIBLE_COMMENTS && (
                                    <span className={styles.commentMore}>
                                        외 {question.commentCount - MAX_VISIBLE_COMMENTS}개 댓글
                                    </span>
                                )}
                            </div>
                        )}

                        {commentOpenId === question.questionId && (
                            <div className={styles.commentInputRow} onClick={e => e.stopPropagation()}>
                                {commentImagePreviews[question.questionId] && (
                                    <div className={styles.imagePreviewWrapper}>
                                        <img src={commentImagePreviews[question.questionId]} alt="미리보기" className={styles.imagePreview} />
                                        <button
                                            className={styles.imageRemoveBtn}
                                            onClick={e => {
                                                e.stopPropagation();
                                                setCommentImages(prev => ({ ...prev, [question.questionId]: null }));
                                                setCommentImagePreviews(prev => ({ ...prev, [question.questionId]: null }));
                                            }}
                                        >✕</button>
                                    </div>
                                )}
                                <div className={styles.commentInputInner}>
                                    <button
                                        className={styles.commentPlusBtn}
                                        onClick={e => {
                                            e.stopPropagation();
                                            if (!commentFileRefs.current[question.questionId]) {
                                                commentFileRefs.current[question.questionId] = document.createElement('input');
                                                commentFileRefs.current[question.questionId].type = 'file';
                                                commentFileRefs.current[question.questionId].accept = 'image/*';
                                                commentFileRefs.current[question.questionId].onchange = (ev) => handleCommentImageSelect(ev, question.questionId);
                                            }
                                            commentFileRefs.current[question.questionId].click();
                                        }}
                                    >+</button>
                                    <input
                                        className={styles.commentInput}
                                        placeholder="댓글을 입력해주세요..."
                                        value={commentInputs[question.questionId] || ''}
                                        onChange={e => handleCommentChange(question.questionId, e.target.value)}
                                        onKeyDown={e => { if (e.key === 'Enter') handleCommentSubmit(e, question.questionId); }}
                                        onPaste={e => handleCommentPaste(e, question.questionId)}
                                        autoFocus
                                    />
                                    <button className={styles.submitBtn}
                                        onClick={e => handleCommentSubmit(e, question.questionId)}>
                                        <SumitBtn />
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            <div className={styles.bottomCover} />

            {!isPast && (
                <div className={styles.newQuestionBar}>
                    {submitError && <p className={styles.errorMsg}>{submitError}</p>}
                    {imagePreview && (
                        <div className={styles.imagePreviewWrapper}>
                            <img src={imagePreview} alt="미리보기" className={styles.imagePreview} />
                            <button
                                className={styles.imageRemoveBtn}
                                onClick={() => { setSelectedImage(null); setImagePreview(null); }}
                            >✕</button>
                        </div>
                    )}
                    <div className={styles.newQuestionInputRow}>
                        {!isStaff && (  // ← 스태프일 때 + 버튼 숨김
                            <>
                                <button
                                    className={styles.newQuestionPlus}
                                    onClick={() => fileInputRef.current?.click()}
                                >+</button>
                                <input
                                    type="file"
                                    accept="image/*"
                                    ref={fileInputRef}
                                    style={{ display: 'none' }}
                                    onChange={handleImageSelect}
                                />
                            </>
                        )}
                        <input
                            className={styles.newQuestionInput}
                            placeholder={isStaff ? '부원들의 이해도를 체크해보세요' : '질문을 남겨주세요...'}
                            value={newQuestion}
                            onChange={e => setNewQuestion(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === 'Enter') isStaff ? handleNewUnderstandCheck() : handleNewQuestion();
                            }}
                            disabled={isSubmitting}
                        />
                        <button
                            className={styles.newQuestionSubmit}
                            onClick={isStaff ? handleNewUnderstandCheck : handleNewQuestion}
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? '⏳' : <SumitBtn />}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default QnAListPage;