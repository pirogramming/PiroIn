import { useState } from 'react';
import styles from './QnAListPage.module.css';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import {
    CommentImoji,
    MeCuriousToo,
    StaffCheck,
    SortBtn,
    OBtn,
    XBtn,
    CommentCommentArraw,
    SumitBtn,
} from '../../components/qna_svg';

const UNDERSTAND = ['이해했다', '성공했다'];

// 댓글 최대 표시 개수 (카드에서 항상 노출)
const MAX_VISIBLE_COMMENTS = 3;

// 질문 목록 데이터
const MOCK_QUESTIONS = [
    {
        id: 1,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,    // 내가 좋아요 눌렀는지
        isSolved: false, // 운영진이 해결 표시했는지
        image: null,      // 첨부 이미지 없음
        comments: [],     // 댓글 없음
    },
    {
        id: 2,
        text: '오류났어요',
        likes: 7,
        iLiked: false,
        isSolved: false,
        image: 'https://dora-guide.com/wp-content/uploads/2019/11/Visual-studio-code-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%82%AC%EC%9A%A9%EB%B2%95.png',
        comments: [
            { id: 1, author: '운영진1', isStaff: true, content: '사진 참고하세요' },
            { id: 2, author: '작성자', isStaff: false, content: '감사합니다' },
            { id: 3, author: '익명1', isStaff: false, content: '감사합니다' },
        ],
    },
    {
        id: 3,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        isSolved: false,
        image: null,
        comments: [],
    },
    {
        id: 4,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        isSolved: true,
        image: null,
        comments: [],
    },
    {
        id: 5,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        isSolved: true,
        image: null,
        comments: [],
    },
];





function QnAListPage({
    sessionTitle = '1주차 화요일 오전 세션(HTML/CSS)',
    sessionId = 1,
    isStaff = true,
    onBack,
    onCardClick, // 카드 클릭 시 상세 페이지 이동 (questionId를 인자로 받아)
}) {



    // 현재 보고 있는 이해도 체크 인덱스 (0 = '이해했다')
    const [understandIndex, setUnderstandIndex] = useState(0);

    // 저도 궁금해요 필터 켜져 있는지(부원)
    const [filterCurious, setFilterCurious] = useState(false);

    // 미해결 질문 필터 켜져 있는지(운영진)
    const [filterUnsolved, setFilterUnsolved] = useState(false);

    // 정렬 방식
    const [sortOrder, setSortOrder] = useState('정렬');

    // 정렬 드롭다운 열려 있는지
    const [showSortMenu, setShowSortMenu] = useState(false);

    // 질문 목록 (좋아요 토글 등 변경사항 반영하려고 state로 관리)
    const [questions, setQuestions] = useState(MOCK_QUESTIONS);

    // 내가 이해했는지 여부: true = O 누름, false = X 누름, null = 아직 안 누름
    const [myUnderstand, setMyUnderstand] = useState(null);

    // 댓글 입력창이 열려 있는 질문 id
    // null이면 아무 질문도 댓글 입력창이 안 열려 있는 상태
    // '댓글달기' 버튼을 누른 질문의 id가 들어옴
    const [commentOpenId, setCommentOpenId] = useState(null);

    // 각 질문별 댓글 입력창 텍스트 (객체로 관리: { 질문id: 입력된텍스트 })
    const [commentInputs, setCommentInputs] = useState({});

    // 새 질문 입력창 텍스트x
    const [newQuestion, setNewQuestion] = useState('');

    // API 요청 중인지 여부 (true면 버튼 비활성화 → 중복 제출 방지)
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 에러 메시지 (null이면 에러 없음)
    const [submitError, setSubmitError] = useState(null);


    // 이해도
    const goPrevUnderstand = () => {
        if (understandIndex > 0) setUnderstandIndex(prev => prev - 1);
    };
    const goNextUnderstand = () => {
        if (understandIndex < UNDERSTAND.length - 1) setUnderstandIndex(prev => prev + 1);
    };

    // 질문
    const toggleLike = (e, id) => {
        e.stopPropagation();

        setQuestions(prev =>
            prev.map(q =>
                q.id === id
                    ? {
                        ...q,
                        iLiked: !q.iLiked,
                        likes: q.iLiked ? q.likes - 1 : q.likes + 1,
                    }
                    : q
            )
        );
    };

    const toggleCommentInput = (e, questionId) => {
        e.stopPropagation();
        setCommentOpenId(prev => prev === questionId ? null : questionId);
    };

    const handleCommentChange = (questionId, value) => {
        setCommentInputs(prev => ({
            ...prev,
            [questionId]: value,
        }));
    };

    const handleCommentSubmit = (e, questionId) => {
        e.stopPropagation();

        const text = (commentInputs[questionId] || '').trim();
        if (!text) return;

        setQuestions(prev =>
            prev.map(q =>
                q.id === questionId
                    ? {
                        ...q,
                        comments: [
                            ...q.comments,
                            { id: Date.now(), author: '나', isStaff: false, content: text },
                        ],
                    }
                    : q
            )
        );
        setCommentInputs(prev => ({ ...prev, [questionId]: '' }));
        setCommentOpenId(null);
    };



    const handleNewQuestion = async () => {
        const text = newQuestion.trim();
        if (!text) return;

        setIsSubmitting(true);
        setSubmitError(null);

        try {
            setQuestions(prev => [
                { id: Date.now(), text, likes: 0, iLiked: false, image: null, comments: [] },
                ...prev,
            ]);
            setNewQuestion('');

        } catch (error) {
            console.error('질문 등록 실패:', error);
            setSubmitError('질문 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false);
        }
    };




    const currentUnderstand = UNDERSTAND[understandIndex];

    // 저도 궁금해요 필터가 켜져 있으면 필터링
    const displayedQuestions = (() => {
        if (isStaff && filterUnsolved) return questions.filter(q => q.comments.length === 0 && !q.isSolved);
        if (!isStaff && filterCurious) return questions.filter(q => q.iLiked);
        return questions;
    })();






    return (
        // 상단
        <div className={styles.page}>

            <h1 className={styles.title}>{sessionTitle}</h1>

            <div className={styles.filterRow}>
                {isStaff ? (
                    <label className={styles.curiousLabel}>
                        <input
                            type="checkbox"
                            checked={filterUnsolved}
                            onChange={e => setFilterUnsolved(e.target.checked)}
                            className={styles.curiousCheckbox}
                        />
                        미해결 질문
                    </label>
                ) : (
                    <label className={styles.curiousLabel}>
                        <input
                            type="checkbox"
                            checked={filterCurious}
                            onChange={e => setFilterCurious(e.target.checked)}
                            className={styles.curiousCheckbox}
                        />
                        저도 궁금해요
                    </label>
                )}

                <div className={styles.sortWrapper}>
                    <button
                        className={styles.sortBtn}
                        onClick={() => setShowSortMenu(prev => !prev)} // 토글
                    >
                        {sortOrder} <SortBtn />
                    </button>

                    {showSortMenu && (
                        <ul className={styles.sortMenu}>
                            {['기본', '최신순', '저도궁금해요순'].map(option => (
                                <li
                                    key={option}
                                    className={styles.sortOption}
                                    onClick={() => {
                                        setSortOrder(option);
                                        setShowSortMenu(false);
                                    }}
                                >
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

                <button className={styles.arrowBtn} onClick={goPrevUnderstand} disabled={understandIndex === 0}>
                    <FiChevronLeft size={30} />
                </button>
                <span className={styles.understandName}>
                    {currentUnderstand}
                    <span className={styles.understandCount}> (13/29)</span>

                </span>
                <button
                    className={`${styles.oxBtn} ${styles.oxO} ${myUnderstand === true ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === true ? null : true)}
                    title="이해했어요"
                    disabled={isStaff}
                >
                    <OBtn />
                    {isStaff && <span className={styles.oxCount}>7</span>}
                </button>
                <button
                    className={`${styles.oxBtn} ${styles.oxX} ${myUnderstand === false ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === false ? null : false)}
                    title="모르겠어요"
                    disabled={isStaff}
                >
                    <XBtn />
                    {isStaff && <span className={styles.oxCount}>6</span>}
                </button>
                <button className={styles.arrowBtn} onClick={goNextUnderstand} disabled={understandIndex === UNDERSTAND.length - 1}>
                    <FiChevronRight size={30} />
                </button>
            </div>


            {/* ── 질문 목록 ── */}
            <div className={styles.questionList}>
                {displayedQuestions.map(question => (
                    <div
                        key={question.id}
                        className={styles.questionCard}
                        onClick={() => onCardClick?.(question.id)}
                    >

                        <div className={styles.questionHeader}>
                            <span className={styles.qIcon}>Q.</span>
                            <span className={styles.questionText}>{question.text}</span>

                            <div className={styles.questionActions}>
                                <button
                                    className={`${styles.likeBtn} ${question.iLiked ? styles.liked : ''}`}
                                    onClick={e => toggleLike(e, question.id)}
                                >
                                    <MeCuriousToo />{question.likes}
                                </button>
                                <button
                                    className={styles.commentBtn}
                                    onClick={e => toggleCommentInput(e, question.id)}
                                >
                                    <CommentImoji />
                                    &nbsp;댓글달기
                                </button>
                            </div>
                        </div>
                        {question.image && (
                            <img
                                src={question.image}
                                alt="첨부 이미지"
                                className={styles.questionImage}
                                onClick={e => e.stopPropagation()}
                            />
                        )}


                        {question.comments.length > 0 && (
                            <div className={styles.commentPreview}>
                                {question.comments.slice(0, MAX_VISIBLE_COMMENTS).map(comment => (
                                    <div key={comment.id} className={styles.commentWrapper}>
                                        <span className={styles.commentAuthor}>
                                            {comment.author}
                                            {comment.isStaff && (
                                                <span className={styles.staffBadge}><StaffCheck /></span>
                                            )}
                                        </span>
                                        {/* 댓글 내용 */}
                                        <div className={styles.commentItem}>
                                            <div className={styles.commentContent}>
                                                <CommentCommentArraw /> {comment.content}
                                            </div>
                                        </div>
                                    </div>
                                ))}


                                {question.comments.length > MAX_VISIBLE_COMMENTS && (
                                    <span className={styles.commentMore}>
                                        외 {question.comments.length - MAX_VISIBLE_COMMENTS}개 댓글
                                    </span>
                                )}
                            </div>
                        )}
                        {commentOpenId === question.id && (
                            <div
                                className={styles.commentInputRow}
                                onClick={e => e.stopPropagation()}
                            >
                                <input
                                    className={styles.commentInput}
                                    placeholder="댓글을 입력해주세요..."
                                    value={commentInputs[question.id] || ''}
                                    onChange={e => handleCommentChange(question.id, e.target.value)}
                                    // 엔터 누르면 제출
                                    onKeyDown={e => {
                                        if (e.key === 'Enter') handleCommentSubmit(e, question.id);
                                    }}
                                    autoFocus
                                />
                                <button
                                    className={styles.submitBtn}
                                    onClick={e => handleCommentSubmit(e, question.id)}
                                >
                                    <SumitBtn />
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>


            <div className={styles.bottomCover} />
            <div className={styles.newQuestionBar}>

                {submitError && (
                    <p className={styles.errorMsg}>{submitError}</p>
                )}

                <div className={styles.newQuestionInputRow}>
                    <button className={styles.newQuestionPlus}>+</button>
                    <input
                        className={styles.newQuestionInput}
                        placeholder="질문을 남겨주세요..."
                        value={newQuestion}
                        onChange={e => setNewQuestion(e.target.value)}
                        onKeyDown={e => {
                            if (e.key === 'Enter') handleNewQuestion();
                        }}
                        disabled={isSubmitting}
                    />
                    <button
                        className={styles.newQuestionSubmit}
                        onClick={handleNewQuestion}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '⏳' : <SumitBtn />}
                    </button>
                </div>
            </div>

        </div>
    );
}

export default QnAListPage;