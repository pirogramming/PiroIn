import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import styles from './QnAListPage.module.css';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import { authFetch } from '../../utils/Api';
import { subscribeQuestionEvents } from '../../utils/sse';
import {
    CommentImoji, MeCuriousToo, SortBtn,
    OBtn, XBtn, CommentCommentArraw, SumitBtn, StaffCheck, ImgPreview,
    DAY_PART_KO, DAY_OF_WEEK_KO, uploadImages,
} from '../../utils/qnaUtils';

const MAX_VISIBLE_COMMENTS = 3;
const POPULAR_LIKE_THRESHOLD = 5;

const getCreatedAtTime = (question) => new Date(question.createdAt ?? 0).getTime();

const sortQuestionGroups = (groups) => ({
    popularQuestions: [...groups.popularQuestions].sort(
        (a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0) || getCreatedAtTime(b) - getCreatedAtTime(a)
    ),
    unresolvedQuestions: [...groups.unresolvedQuestions].sort(
        (a, b) => getCreatedAtTime(b) - getCreatedAtTime(a)
    ),
    resolvedQuestions: [...groups.resolvedQuestions].sort(
        (a, b) => getCreatedAtTime(b) - getCreatedAtTime(a)
    ),
});

const getQuestionGroupKey = (question) => {
    if (question.isResolved) return 'resolvedQuestions';
    if ((question.likeCount ?? 0) >= POPULAR_LIKE_THRESHOLD) return 'popularQuestions';
    return 'unresolvedQuestions';
};

const regroupQuestions = (questions) => {
    const groups = {
        popularQuestions: [],
        unresolvedQuestions: [],
        resolvedQuestions: [],
    };
    const seenQuestionIds = new Set();

    questions.forEach(question => {
        if (seenQuestionIds.has(question.questionId)) return;
        seenQuestionIds.add(question.questionId);
        groups[getQuestionGroupKey(question)].push(question);
    });

    return sortQuestionGroups(groups);
};

const updateQuestionGroupsByCommentEvent = (groups, eventData) => {
    if (!eventData?.questionId) return groups;

    let hasUpdatedQuestion = false;
    const questions = [
        ...groups.popularQuestions,
        ...groups.unresolvedQuestions,
        ...groups.resolvedQuestions,
    ].map(question => {
        if (question.questionId !== eventData.questionId) return question;

        hasUpdatedQuestion = true;
        const isResolved = eventData.isResolved ?? question.isResolved;

        return {
            ...question,
            isResolved,
            isPopular: !isResolved && (question.likeCount ?? 0) >= POPULAR_LIKE_THRESHOLD,
            commentCount: eventData.commentCount ?? question.commentCount,
            previewComments: eventData.previewComments ?? question.previewComments,
        };
    });

    return hasUpdatedQuestion ? regroupQuestions(questions) : groups;
};

const updateQuestionGroupsByQuestionEvent = (groups, eventData) => {
    if (!eventData?.questionId) return groups;

    const questions = [
        ...groups.popularQuestions,
        ...groups.unresolvedQuestions,
        ...groups.resolvedQuestions,
    ];

    if (eventData.isDeleted) {
        return regroupQuestions(questions.filter(question => question.questionId !== eventData.questionId));
    }

    let hasUpdatedQuestion = false;
    const updatedQuestions = questions.map(question => {
        if (question.questionId !== eventData.questionId) return question;

        hasUpdatedQuestion = true;
        const isResolved = eventData.isResolved ?? question.isResolved;
        const likeCount = eventData.likeCount ?? question.likeCount;

        return {
            ...question,
            content: eventData.content ?? question.content,
            isResolved,
            isPopular: !isResolved && (likeCount ?? 0) >= POPULAR_LIKE_THRESHOLD,
            likeCount,
            iLiked: eventData.isLiked ?? question.iLiked,
            isLiked: eventData.isLiked ?? question.isLiked,
        };
    });

    return hasUpdatedQuestion ? regroupQuestions(updatedQuestions) : groups;
};

const addQuestionToGroups = (groups, question) => {
    if (!question?.questionId) return groups;

    const existingQuestions = [
        ...groups.popularQuestions,
        ...groups.unresolvedQuestions,
        ...groups.resolvedQuestions,
    ];
    const alreadyExists = existingQuestions.some(item => item.questionId === question.questionId);

    if (alreadyExists) return groups;
    return regroupQuestions([question, ...existingQuestions]);
};

const buildUnderstandingCheckFromEvent = (eventData) => ({
    checkId: eventData.checkId,
    content: eventData.content,
    respondedCount: eventData.respondedCount ?? 0,
    attendanceCount: eventData.attendanceCount ?? 0,
    understoodCount: eventData.understoodCount ?? 0,
    notUnderstoodCount: eventData.notUnderstoodCount ?? 0,
    selectedChoice: null,
    createdAt: eventData.createdAt,
});

const updateCurrentUnderstandingCounts = (understanding, eventData) => {
    if (!understanding?.current || understanding.current.checkId !== eventData?.checkId) {
        return understanding;
    }

    return {
        ...understanding,
        current: {
            ...understanding.current,
            respondedCount: eventData.respondedCount ?? understanding.current.respondedCount,
            attendanceCount: eventData.attendanceCount ?? understanding.current.attendanceCount,
            understoodCount: eventData.understoodCount ?? understanding.current.understoodCount,
            notUnderstoodCount: eventData.notUnderstoodCount ?? understanding.current.notUnderstoodCount,
        },
    };
};

function QnAListPage() {
    const { sessionId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const isPast = location.state?.status === 'AFTER_SESSION';
    const isStaff = localStorage.getItem('role') === 'ADMIN';

    // ── 세션 / 이해도 상태 ──────────────────────────
    const [sessionTitle, setSessionTitle] = useState('');
    const [understanding, setUnderstanding] = useState(null);
    const [understandingIndex, setUnderstandingIndex] = useState(0);
    const [myChoices, setMyChoices] = useState({});
    const understandingRef = useRef(null);

    // ── 질문 목록 상태 ───────────────────────────────
    const [popularQuestions, setPopularQuestions] = useState([]);
    const [unresolvedQuestions, setUnresolvedQuestions] = useState([]);
    const [resolvedQuestions, setResolvedQuestions] = useState([]);
    const questionGroupsRef = useRef({
        popularQuestions: [],
        unresolvedQuestions: [],
        resolvedQuestions: [],
    });

    // ── 필터 / 정렬 상태 ─────────────────────────────
    const [filterCurious, setFilterCurious] = useState(false);
    const [filterUnsolved, setFilterUnsolved] = useState(false);
    const [sortOrder, setSortOrder] = useState('정렬');
    const [showSortMenu, setShowSortMenu] = useState(false);

    // ── 댓글 입력 상태 ───────────────────────────────
    const [commentOpenId, setCommentOpenId] = useState(null);
    const [commentInputs, setCommentInputs] = useState({});
    // 질문별 댓글 이미지 여러 장: { [questionId]: File[] }
    const [commentImages, setCommentImages] = useState({});
    // 질문별 댓글 이미지 미리보기: { [questionId]: string[] }
    const [commentImagePreviews, setCommentImagePreviews] = useState({});
    const commentFileRefs = useRef({});

    // ── 새 질문 / 이해도 입력 상태 ──────────────────
    const [newQuestion, setNewQuestion] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    // 질문 이미지 여러 장
    const [selectedImages, setSelectedImages] = useState([]);
    const [imagePreviews, setImagePreviews] = useState([]);
    const fileInputRef = useRef(null);

    const applyQuestionGroups = useCallback((groups) => {
        questionGroupsRef.current = groups;
        setPopularQuestions(groups.popularQuestions);
        setUnresolvedQuestions(groups.unresolvedQuestions);
        setResolvedQuestions(groups.resolvedQuestions);
    }, []);

    useEffect(() => {
        understandingRef.current = understanding;
    }, [understanding]);

    useEffect(() => {
        questionGroupsRef.current = {
            popularQuestions,
            unresolvedQuestions,
            resolvedQuestions,
        };
    }, [popularQuestions, unresolvedQuestions, resolvedQuestions]);

    // ── 질문 목록 불러오기 ───────────────────────────
    const fetchQuestions = useCallback(async (index) => {
        try {
            const res = await authFetch(`/api/sessions/${sessionId}/questions?understandingIndex=${index}`);
            if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
            const json = await res.json();
            if (!json.isSuccess) throw new Error(json.message);

            const { session, understanding, questions } = json.result;

            setSessionTitle(`${session.week}주차 ${DAY_OF_WEEK_KO[session.dayOfWeek]}요일 ${DAY_PART_KO[session.dayPart]} (${session.title})`);
            setUnderstanding(understanding);
            const currentCheck = understanding?.current;
            if (currentCheck?.checkId) {
                setMyChoices(prev => ({
                    ...prev,
                    [currentCheck.checkId]: currentCheck.selectedChoice ?? null,
                }));
            }

            const allQ = [
                ...(questions.popularQuestions ?? []),
                ...(questions.unresolvedQuestions ?? []),
                ...(questions.resolvedQuestions ?? []),
            ];

            // 질문 이미지 blob URL 변환 (여러 장: imageUrls 배열)
            const withBlob = await Promise.all(
                allQ.map(async (q) => {
                    const rawUrls = q.imageUrls ?? [];
                    const blobUrls = await Promise.all(
                        rawUrls.map(async (url) => {
                            try {
                                const imgRes = await authFetch(url);
                                const blob = await imgRes.blob();
                                return URL.createObjectURL(blob);
                            } catch {
                                return null;
                            }
                        })
                    );
                    return { ...q, iLiked: q.isLiked, imageUrls: blobUrls.filter(Boolean) };
                })
            );

            const idSet = (list) => new Set(list.map(q => q.questionId));
            const popularIds = idSet(questions.popularQuestions ?? []);
            const unresolvedIds = idSet(questions.unresolvedQuestions ?? []);
            const resolvedIds = idSet(questions.resolvedQuestions ?? []);

            applyQuestionGroups({
                popularQuestions: withBlob.filter(q => popularIds.has(q.questionId)),
                unresolvedQuestions: withBlob.filter(q => unresolvedIds.has(q.questionId)),
                resolvedQuestions: withBlob.filter(q => resolvedIds.has(q.questionId)),
            });

        } catch (err) {
            console.error('질문 불러오기 실패:', err);
        }
    }, [sessionId, applyQuestionGroups]);

    useEffect(() => {
        if (sessionId) fetchQuestions(understandingIndex);
    }, [sessionId, understandingIndex, fetchQuestions]);

    const handleCommentCreatedEvent = useCallback((eventData) => {
        const nextGroups = updateQuestionGroupsByCommentEvent(questionGroupsRef.current, eventData);
        applyQuestionGroups(nextGroups);
    }, [applyQuestionGroups]);

    const buildQuestionFromCreatedEvent = useCallback(async (eventData) => {
        if (!eventData?.questionId) return null;

        // SSE 이벤트의 imageUrls 배열을 blob URL로 변환
        const rawUrls = eventData.imageUrls ?? [];
        const blobUrls = await Promise.all(
            rawUrls.map(async (url) => {
                try {
                    const imgRes = await authFetch(url);
                    const blob = await imgRes.blob();
                    return URL.createObjectURL(blob);
                } catch {
                    return null;
                }
            })
        );

        return {
            questionId: eventData.questionId,
            content: eventData.content,
            imageUrls: blobUrls.filter(Boolean),
            isResolved: false,
            isPopular: false,
            isLiked: false,
            isMine: false,
            iLiked: false,
            likeCount: eventData.likeCount ?? 0,
            commentCount: eventData.commentCount ?? 0,
            previewComments: [],
            createdAt: eventData.createdAt,
        };
    }, []);

    const handleQuestionCreatedEvent = useCallback(async (eventData) => {
        const createdQuestion = await buildQuestionFromCreatedEvent(eventData);
        if (!createdQuestion) return;

        const nextGroups = addQuestionToGroups(questionGroupsRef.current, createdQuestion);
        applyQuestionGroups(nextGroups);
    }, [applyQuestionGroups, buildQuestionFromCreatedEvent]);

    const handleQuestionUpdatedEvent = useCallback((eventData) => {
        const nextGroups = updateQuestionGroupsByQuestionEvent(questionGroupsRef.current, eventData);
        applyQuestionGroups(nextGroups);
    }, [applyQuestionGroups]);

    const handleUnderstandingCheckCreatedEvent = useCallback((eventData) => {
        if (!eventData?.checkId) return;
        if (understandingRef.current?.current?.checkId === eventData.checkId) return;

        if (understandingIndex === 0) {
            setUnderstanding(prev => {
                if (prev?.current?.checkId === eventData.checkId) return prev;

                const previousTotalCount = prev?.totalCount ?? 0;
                const nextUnderstanding = {
                    current: buildUnderstandingCheckFromEvent(eventData),
                    currentIndex: 0,
                    totalCount: previousTotalCount + 1,
                    hasOlder: previousTotalCount > 0,
                    hasNewer: false,
                };
                understandingRef.current = nextUnderstanding;
                return nextUnderstanding;
            });
            return;
        }

        setUnderstanding(prev => {
            if (prev?.current?.checkId === eventData.checkId) return prev;

            const nextTotalCount = (prev?.totalCount ?? understandingIndex + 1) + 1;
            const nextCurrentIndex = (prev?.currentIndex ?? understandingIndex) + 1;
            const nextUnderstanding = {
                ...(prev ?? {}),
                currentIndex: nextCurrentIndex,
                totalCount: nextTotalCount,
                hasOlder: nextCurrentIndex < nextTotalCount - 1,
                hasNewer: true,
            };
            understandingRef.current = nextUnderstanding;
            return nextUnderstanding;
        });
        setUnderstandingIndex(prev => prev + 1);
    }, [understandingIndex]);

    const handleUnderstandingResponseUpdatedEvent = useCallback((eventData) => {
        setUnderstanding(prev => {
            const nextUnderstanding = updateCurrentUnderstandingCounts(prev, eventData);
            understandingRef.current = nextUnderstanding;
            return nextUnderstanding;
        });
    }, []);

    const handleQuestionEvent = useCallback((message) => {
        const { event, data } = message;

        switch (event) {
            case 'connected':
                console.debug('질문방 SSE 연결 완료');
                break;
            case 'comment-created':
                handleCommentCreatedEvent(data);
                break;
            case 'comment-updated':
                handleCommentCreatedEvent(data);
                break;
            case 'question-created':
                void handleQuestionCreatedEvent(data);
                break;
            case 'question-updated':
                handleQuestionUpdatedEvent(data);
                break;
            case 'understanding-check-created':
                handleUnderstandingCheckCreatedEvent(data);
                break;
            case 'understanding-response-updated':
                handleUnderstandingResponseUpdatedEvent(data);
                break;
            default:
                console.debug('알 수 없는 질문방 SSE 이벤트 수신:', message);
                break;
        }
    }, [
        handleCommentCreatedEvent,
        handleQuestionCreatedEvent,
        handleQuestionUpdatedEvent,
        handleUnderstandingCheckCreatedEvent,
        handleUnderstandingResponseUpdatedEvent,
    ]);

    useEffect(() => {
        if (!sessionId) {
            return undefined;
        }

        return subscribeQuestionEvents(sessionId, {
            onOpen: () => {
                console.debug('질문방 SSE 연결 열림');
            },
            onEvent: handleQuestionEvent,
            onError: (error) => {
                console.error('질문방 SSE 연결 실패:', error);
            },
        });
    }, [sessionId, handleQuestionEvent]);

    // ── 이해도 네비게이션 ────────────────────────────
    const goPrevUnderstand = () => {
        if (understanding?.hasOlder) setUnderstandingIndex(prev => prev + 1);
    };
    const goNextUnderstand = () => {
        if (understanding?.hasNewer) setUnderstandingIndex(prev => prev - 1);
    };

    // ── 이해도 O/X 선택 ──────────────────────────────
    const handleUnderstandChoice = async (choice) => {
        if (!understanding?.current?.checkId) return;
        const checkId = understanding.current.checkId;
        const previousChoice = myChoices[checkId] ?? null;
        const newChoice = previousChoice === choice ? null : choice;
        setMyChoices(prev => ({ ...prev, [checkId]: newChoice }));
        try {
            const res = await authFetch(
                `/api/sessions/${sessionId}/understanding-checks/${checkId}/responses`,
                { method: 'POST', body: JSON.stringify({ choice }) }
            );
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (!json.isSuccess) throw new Error(json.message);
            setMyChoices(prev => ({ ...prev, [checkId]: json.result.selectedChoice ?? null }));
            setUnderstanding(prev => ({
                ...prev,
                current: {
                    ...prev.current,
                    understoodCount: json.result.understoodCount,
                    notUnderstoodCount: json.result.notUnderstoodCount,
                    attendanceCount: json.result.attendanceCount,
                    respondedCount: json.result.respondedCount,
                    selectedChoice: json.result.selectedChoice,
                }
            }));
        } catch (err) {
            setMyChoices(prev => ({ ...prev, [checkId]: previousChoice }));
            console.error('이해도 응답 실패:', err);
        }
    };

    // ── 좋아요 토글 ──────────────────────────────────
    const toggleLike = async (e, questionId) => {
        e.stopPropagation();
        if (isPast) return;
        try {
            const res = await authFetch(`/api/questions/${questionId}/like`, { method: 'POST' });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                const nextGroups = updateQuestionGroupsByQuestionEvent(questionGroupsRef.current, {
                    questionId,
                    likeCount: json.result.likeCount,
                    isLiked: json.result.isLiked,
                });
                applyQuestionGroups(nextGroups);
            }
        } catch (err) {
            console.error('좋아요 실패:', err);
        }
    };

    // ── 댓글 입력창 토글 ─────────────────────────────
    const toggleCommentInput = (e, questionId) => {
        e.stopPropagation();
        if (isPast) return;
        setCommentOpenId(prev => prev === questionId ? null : questionId);
    };

    const handleCommentChange = (questionId, value) => {
        setCommentInputs(prev => ({ ...prev, [questionId]: value }));
    };

    // ── 댓글 등록 ────────────────────────────────────
    const handleCommentSubmit = async (e, questionId) => {
        e.stopPropagation();
        const text = (commentInputs[questionId] || '').trim();
        const images = commentImages[questionId] ?? [];
        if (!text && images.length === 0) return;
        try {
            let imageUrls = [];
            if (images.length > 0) {
                imageUrls = await uploadImages(images);
            }
            const res = await authFetch(`/api/questions/${questionId}/comments`, {
                method: 'POST',
                body: JSON.stringify({ content: text, parentCommentId: null, imageUrls }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                // NOTE: 댓글 작성으로 인한 '해결됨' 자동 전환 없음 (해결 처리는 운영진 수동 조작만 허용).
                // previewComments / commentCount UI 갱신도 SSE comment-created 이벤트에서 단일 처리.
                // 여기서 직접 상태를 업데이트하면 SSE 이벤트와 중복되어 댓글이 2개 표시되므로 제거.
                setCommentInputs(prev => ({ ...prev, [questionId]: '' }));
                setCommentImages(prev => ({ ...prev, [questionId]: [] }));
                setCommentImagePreviews(prev => ({ ...prev, [questionId]: [] }));
                setCommentOpenId(null);
            }
        } catch (err) {
            console.error('댓글 등록 실패:', err);
        }
    };

    // ── 댓글 이미지 선택 / 붙여넣기 ─────────────────
    const handleCommentImageSelect = (e, questionId) => {
        const files = Array.from(e.target.files);
        if (!files.length) return;
        const prev = commentImages[questionId] ?? [];
        const merged = [...prev, ...files].slice(0, 5);
        setCommentImages(p => ({ ...p, [questionId]: merged }));
        setCommentImagePreviews(p => ({ ...p, [questionId]: merged.map(f => URL.createObjectURL(f)) }));
        e.target.value = '';
    };

    const handleCommentRemoveImage = (questionId, idx) => {
        const prev = commentImages[questionId] ?? [];
        const next = prev.filter((_, i) => i !== idx);
        setCommentImages(p => ({ ...p, [questionId]: next }));
        setCommentImagePreviews(p => ({ ...p, [questionId]: next.map(f => URL.createObjectURL(f)) }));
    };

    const handleCommentPaste = (e, questionId) => {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                const file = item.getAsFile();
                if (file) {
                    const prev = commentImages[questionId] ?? [];
                    const merged = [...prev, file].slice(0, 5);
                    setCommentImages(p => ({ ...p, [questionId]: merged }));
                    setCommentImagePreviews(p => ({ ...p, [questionId]: merged.map(f => URL.createObjectURL(f)) }));
                }
                break;
            }
        }
    };

    // ── 질문 이미지 선택 (여러 장) ───────────────────
    const handleImageSelect = (e) => {
        const files = Array.from(e.target.files);
        if (!files.length) return;
        const merged = [...selectedImages, ...files].slice(0, 5);
        setSelectedImages(merged);
        setImagePreviews(merged.map(f => URL.createObjectURL(f)));
        e.target.value = '';
    };

    const handleRemoveImage = (idx) => {
        const next = selectedImages.filter((_, i) => i !== idx);
        setSelectedImages(next);
        setImagePreviews(next.map(f => URL.createObjectURL(f)));
    };

    const handleNewQuestionPaste = (e) => {
        if (isStaff) return; // 이해도 체크 입력창에는 이미지 첨부 없음
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

    // ── 새 질문 등록 ─────────────────────────────────
    const handleNewQuestion = async () => {
        const text = newQuestion.trim();
        if (!text && selectedImages.length === 0) return;
        setIsSubmitting(true);
        setSubmitError(null);
        try {
            let imageUrls = [];
            if (selectedImages.length > 0) {
                imageUrls = await uploadImages(selectedImages);
            }
            const res = await authFetch(`/api/sessions/${sessionId}/questions`, {
                method: 'POST',
                body: JSON.stringify({ content: text, imageUrls }),
            });
            if (!res.ok) throw new Error();
            const json = await res.json();
            if (json.isSuccess) {
                setNewQuestion('');
                setSelectedImages([]);
                setImagePreviews([]);
                fetchQuestions(understandingIndex);
            }
        } catch (err) {
            console.error('질문 등록 실패:', err);
            setSubmitError('질문 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false);
        }
    };

    // ── 이해도 체크 등록 (운영진 전용) ──────────────
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

    // ── 질문 목록 필터 / 정렬 ────────────────────────
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

    useEffect(() => {
        document.title = "Q&A | PIROIN";
    }, []);

    return (
        <div className={styles.page}>
            <h1 className={styles.title}>{sessionTitle}</h1>

            {/* ── 필터 / 정렬 행 ── */}
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

            {/* ── 이해도 바 (이해도 체크가 없으면 숨김) ── */}
            {understanding?.current?.checkId != null && (
                <div className={styles.understandBar}>
                    <button className={styles.arrowBtn} onClick={goPrevUnderstand}
                        disabled={!understanding?.hasOlder}>
                        <FiChevronLeft size={30} />
                    </button>
                    <span className={styles.understandName}>
                        {understanding.current.content}
                        <span className={styles.understandCount}>
                            ({understanding.current.respondedCount ?? 0}/
                            {understanding.current.attendanceCount ?? 0})
                        </span>
                    </span>
                    <button
                        className={`${styles.oxBtn} ${styles.oxO} ${currentChoice === 'UNDERSTOOD' ? styles.oxActive : ''}`}
                        onClick={() => handleUnderstandChoice('UNDERSTOOD')}
                        disabled={isStaff || isPast}
                    >
                        <OBtn />
                        {isStaff && <span className={styles.oxCount}>{understanding.current.understoodCount ?? 0}</span>}
                    </button>
                    <button
                        className={`${styles.oxBtn} ${styles.oxX} ${currentChoice === 'NOT_UNDERSTOOD' ? styles.oxActive : ''}`}
                        onClick={() => handleUnderstandChoice('NOT_UNDERSTOOD')}
                        disabled={isStaff || isPast}
                    >
                        <XBtn />
                        {isStaff && <span className={styles.oxCount}>{understanding.current.notUnderstoodCount ?? 0}</span>}
                    </button>
                    <button className={styles.arrowBtn} onClick={goNextUnderstand}
                        disabled={!understanding?.hasNewer}>
                        <FiChevronRight size={30} />
                    </button>
                </div>
            )}

            {/* ── 질문 목록 ── */}
            <div className={styles.questionList}>
                {displayedQuestions.map(question => (
                    <div key={question.questionId}
                        className={`${styles.questionCard} ${question.isResolved ? styles.questionCardResolved : ''}`}
                        onClick={() => navigate(`/sessions/${sessionId}/questions/${question.questionId}`)}>

                        {/* 질문 헤더 */}
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

                        {/* 질문 첨부 이미지 (여러 장) */}
                        {question.imageUrls?.length > 0 && (
                            <div className={styles.questionImages} onClick={e => e.stopPropagation()}>
                                {question.imageUrls.map((url, idx) => (
                                    <img key={idx} src={url} alt={`첨부 이미지 ${idx + 1}`}
                                        className={styles.questionImage} />
                                ))}
                            </div>
                        )}

                        {/* 댓글 미리보기 */}
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
                                                >
                                                    <ImgPreview /><span>사진보기</span>
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

                        {/* 댓글 입력창 */}
                        {commentOpenId === question.questionId && (
                            <div className={styles.commentInputRow} onClick={e => e.stopPropagation()}>
                                {(commentImagePreviews[question.questionId] ?? []).length > 0 && (
                                    <div className={styles.imagePreviewList}>
                                        {(commentImagePreviews[question.questionId] ?? []).map((preview, idx) => (
                                            <div key={idx} className={styles.imagePreviewWrapper}>
                                                <img src={preview} alt={`미리보기 ${idx + 1}`} className={styles.imagePreview} />
                                                <button
                                                    className={styles.imageRemoveBtn}
                                                    onClick={e => {
                                                        e.stopPropagation();
                                                        handleCommentRemoveImage(question.questionId, idx);
                                                    }}
                                                >✕</button>
                                            </div>
                                        ))}
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
                                                commentFileRefs.current[question.questionId].multiple = true;
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
                                        onKeyDown={e => { if (e.key === 'Enter' && !e.nativeEvent.isComposing) handleCommentSubmit(e, question.questionId); }}
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

            {/* ── 하단 입력바 (지난 세션이면 숨김) ── */}
            {!isPast && (
                <div className={styles.newQuestionBar}>
                    {submitError && <p className={styles.errorMsg}>{submitError}</p>}
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
                    <div className={styles.newQuestionInputRow}>
                        {/* 운영진일 때 + 버튼 숨김 */}
                        {!isStaff && (
                            <>
                                <button
                                    className={styles.newQuestionPlus}
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
                            </>
                        )}
                        <input
                            className={`${styles.newQuestionInput} ${isStaff ? styles.newQuestionInputStaff : ''}`}
                            placeholder={isStaff ? '부원들의 이해도를 체크해보세요' : '질문을 남겨주세요...'}
                            value={newQuestion}
                            onChange={e => setNewQuestion(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === 'Enter' && !e.nativeEvent.isComposing) isStaff ? handleNewUnderstandCheck() : handleNewQuestion();
                            }}
                            onPaste={handleNewQuestionPaste}
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