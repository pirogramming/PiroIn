import { useState } from 'react';
import styles from './QnAListPage.module.css';
import { FiChevronLeft, FiChevronRight, FiMessageSquare } from 'react-icons/fi';


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
        image: null,      // 첨부 이미지 없음
        comments: [],     // 댓글 없음
    },
    {
        id: 2,
        text: '오류났어요',
        likes: 7,
        iLiked: false,
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
        image: null,
        comments: [],
    },
    {
        id: 4,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        image: null,
        comments: [],
    },
    {
        id: 5,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        image: null,
        comments: [],
    },
];


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 메인 컴포넌트
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// props 설명:
//   sessionTitle  → 상단에 보여줄 세션 제목 (QnAMainPage에서 넘겨줄 거야)
//   onBack        → 뒤로가기 버튼 눌렀을 때 실행할 함수 (선택)
//   onCardClick   → 카드 클릭 시 상세 페이지로 이동할 함수 (questionId를 인자로 받아)
//
// 사용 예시 (QnAMainPage에서):
//   <QnAListPage
//     sessionTitle="1주차 화요일 오전 세션(HTML/CSS)"
//     onBack={() => navigate(-1)}
//     onCardClick={(id) => navigate(`/qna/${sessionId}/question/${id}`)}
//   />
//
// React Router 쓴다면 QnAMainPage 카드 onClick에 이렇게:
//   onClick={() => navigate('/qna/1', { state: { title: session.title } })}
//   그리고 이 컴포넌트에서: const { state } = useLocation();

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// API 설정
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━



function QnAListPage({
    sessionTitle = '1주차 화요일 오전 세션(HTML/CSS)',
    sessionId = 1,
    onBack,
    onCardClick, // 카드 클릭 시 상세 페이지 이동 (questionId를 인자로 받아)
}) {

    // ──────────────────────────────────────
    // 📌 useState 사용법:
    //    const [현재값, 값을바꾸는함수] = useState(초기값);
    //    값을바꾸는함수(새값) 호출하면 화면이 자동으로 다시 그려져
    // ──────────────────────────────────────

    // 현재 보고 있는 이해도 체크 인덱스 (0 = '이해했다')
    const [understandIndex, setUnderstandIndex] = useState(0);

    // 저도 궁금해요 필터 켜져 있는지
    const [filterCurious, setFilterCurious] = useState(false);

    // 정렬 방식
    const [sortOrder, setSortOrder] = useState('최신순');

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

    // 새 질문 입력창 텍스트
    const [newQuestion, setNewQuestion] = useState('');

    // API 요청 중인지 여부 (true면 버튼 비활성화 → 중복 제출 방지)
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 에러 메시지 (null이면 에러 없음)
    const [submitError, setSubmitError] = useState(null);


    // ──────────────────────────────────────
    // 이벤트 핸들러 함수들
    // ──────────────────────────────────────

    // < 버튼: 이전 이해도 체크로 이동
    const goPrevUnderstand = () => {
        if (understandIndex > 0) {
            setUnderstandIndex(prev => prev - 1);
        }
    };
    // > 버튼: 다음 이해도 체크로 이동
    const goNextUnderstand = () => {
        setUnderstandIndex(prev => (prev + 1) % UNDERSTAND.length);
    };

    // 좋아요 버튼 토글
    const toggleLike = (e, id) => {
        e.stopPropagation(); // 카드 클릭(상세 이동) 방지

        setQuestions(prev =>
            // prev = 이전 질문 목록 배열
            // map으로 전체 돌면서 해당 id의 질문만 변경
            prev.map(q =>
                q.id === id
                    ? {
                        ...q, // 나머지 필드는 그대로 복사 (spread 연산자)
                        iLiked: !q.iLiked,
                        likes: q.iLiked ? q.likes - 1 : q.likes + 1,
                    }
                    : q // 다른 질문은 그대로
            )
        );
    };

    // '댓글달기' 버튼 클릭: 해당 질문의 댓글 입력창 열기/닫기 토글
    // 이미 열려 있는 질문이면 닫고, 아니면 해당 id로 열기
    const toggleCommentInput = (e, questionId) => {
        e.stopPropagation(); // 카드 클릭(상세 이동) 방지
        setCommentOpenId(prev => prev === questionId ? null : questionId);
    };

    // 댓글 입력창 텍스트 변경
    const handleCommentChange = (questionId, value) => {
        setCommentInputs(prev => ({
            ...prev,             // 기존 다른 질문의 입력값은 유지
            [questionId]: value, // 이 질문의 입력값만 업데이트
        }));
    };

    // 댓글 제출 (엔터 or 버튼 클릭)
    const handleCommentSubmit = (e, questionId) => {
        e.stopPropagation(); // 카드 클릭(상세 이동) 방지

        const text = (commentInputs[questionId] || '').trim();
        if (!text) return; // 빈 댓글은 무시

        setQuestions(prev =>
            prev.map(q =>
                q.id === questionId
                    ? {
                        ...q,
                        comments: [
                            ...q.comments,
                            // Date.now()로 임시 고유 id 생성
                            { id: Date.now(), author: '나', isStaff: false, content: text },
                        ],
                    }
                    : q
            )
        );

        // 입력창 비우기 + 댓글 입력창 닫기
        setCommentInputs(prev => ({ ...prev, [questionId]: '' }));
        setCommentOpenId(null);
    };

    // ✅ 새 질문 등록 함수
    // async: 비동기 함수 선언 키워드 (await를 쓰려면 필요해)
    // 나중에 실제 API 연결할 때 await axios.post(...) 식으로 사용하게 됨
    const handleNewQuestion = async () => {
        const text = newQuestion.trim(); // 앞뒤 공백 제거
        if (!text) return; // 빈 질문이면 무시

        setIsSubmitting(true);  // 버튼 비활성화 (중복 제출 방지)
        setSubmitError(null);   // 이전 에러 메시지 초기화

        try {
            // TODO: 실제 API 연결 시 아래 주석 풀기
            // await axios.post(`/api/sessions/${sessionId}/questions`, { content: text });

            // 지금은 API 없이 바로 화면에 추가 (임시)
            // ...prev = 기존 질문 목록 유지, 새 질문을 맨 앞에 추가
            setQuestions(prev => [
                { id: Date.now(), text, likes: 0, iLiked: false, image: null, comments: [] },
                ...prev,
            ]);
            setNewQuestion(''); // 입력창 비우기

        } catch (error) {
            // 네트워크 오류 or 서버 오류 처리
            console.error('질문 등록 실패:', error);
            setSubmitError('질문 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false); // 버튼 다시 활성화
        }
    };


    // ──────────────────────────────────────
    // 렌더링에 쓸 계산값들
    // ──────────────────────────────────────

    // 현재 이해도 이름
    const currentUnderstand = UNDERSTAND[understandIndex];

    // 저도 궁금해요 필터가 켜져 있으면 필터링
    const displayedQuestions = filterCurious
        ? questions.filter(q => q.iCurious)
        : questions;



    return (
        // 전체 페이지 컨테이너
        // 하단 새 질문 입력창 높이만큼 padding-bottom 줘야 내용이 가려지지 않아
        <div className={styles.page}>

            {/* ── 세션 제목 ── */}
            <h1 className={styles.title}>{sessionTitle}</h1>


            {/* ── 필터/정렬 행 ── */}
            <div className={styles.filterRow}>

                {/* 저도 궁금해요 체크박스 */}
                {/* label 안에 input 넣으면 label 클릭해도 체크박스가 토글돼 */}
                <label className={styles.curiousLabel}>
                    <input
                        type="checkbox"
                        checked={filterCurious}
                        onChange={e => setFilterCurious(e.target.checked)}
                        className={styles.curiousCheckbox}
                    />
                    저도 궁금해요
                </label>

                {/* 정렬 드롭다운 */}
                {/* position: relative인 wrapper로 감싸야 드롭다운 위치가 버튼 아래에 붙어 */}
                <div className={styles.sortWrapper}>
                    <button
                        className={styles.sortBtn}
                        onClick={() => setShowSortMenu(prev => !prev)} // 토글
                    >
                        {sortOrder} ∨
                    </button>

                    {/* showSortMenu가 true일 때만 메뉴를 렌더링 */}
                    {/* JSX에서 조건부 렌더링: {조건 && <보여줄JSX />} */}
                    {showSortMenu && (
                        <ul className={styles.sortMenu}>
                            {['최신순', '좋아요순'].map(option => (
                                <li
                                    key={option} // list를 map할 땐 key 필수
                                    className={styles.sortOption}
                                    onClick={() => {
                                        setSortOrder(option);
                                        setShowSortMenu(false); // 선택하면 메뉴 닫기
                                    }}
                                >
                                    {option}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>


            {/* ── 이해도 체크 바 ── */}
            <div className={styles.understandBar}>

                {/* 이전 이해도 체크 버튼 */}
                <button className={styles.arrowBtn} onClick={goPrevUnderstand} disabled={understandIndex === 0}>
                    <FiChevronLeft size={20} />
                </button>

                {/* 이해도 체크 이름 + 카운트 */}
                <span className={styles.understandName}>
                    {currentUnderstand}
                    <span className={styles.understandCount}> (13/29)</span>
                    {/* TODO: 실제 카운트는 API에서 받아와야 해 */}
                </span>

                {/* O 버튼: 이해했어요 */}
                {/* 눌린 상태면 oxActive 클래스 추가해서 스타일 변경 */}
                {/* JSX에서 여러 클래스 합치기: `${styles.a} ${조건 ? styles.b : ''}` */}
                <button
                    className={`${styles.oxBtn} ${styles.oxO} ${myUnderstand === true ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === true ? null : true)}
                    title="이해했어요"
                >
                    O
                </button>

                {/* X 버튼: 모르겠어요 */}
                <button
                    className={`${styles.oxBtn} ${styles.oxX} ${myUnderstand === false ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === false ? null : false)}
                    title="모르겠어요"
                >
                    ✕
                </button>

                {/* 다음 이해도 체크 버튼 */}
                <button className={styles.arrowBtn} onClick={goNextUnderstand} disabled={understandIndex === UNDERSTAND.length - 1}>
                    <FiChevronRight size={20} />
                </button>
            </div>


            {/* ── 질문 목록 ── */}
            <div className={styles.questionList}>

                {/* displayedQuestions 배열을 map으로 돌면서 카드 하나씩 렌더링 */}
                {displayedQuestions.map(question => (
                    <div
                        key={question.id}
                        className={styles.questionCard}
                        // 카드 클릭 → 상세 페이지 이동
                        // onCardClick prop이 있으면 호출, 없으면 아무것도 안 함
                        onClick={() => onCardClick?.(question.id)}
                    >

                        {/* 질문 헤더 */}
                        <div className={styles.questionHeader}>

                            {/* Q. 아이콘 */}
                            <span className={styles.qIcon}>Q.</span>

                            {/* 질문 텍스트 */}
                            <span className={styles.questionText}>{question.text}</span>

                            {/* 오른쪽 액션 버튼들 */}
                            <div className={styles.questionActions}>

                                {/* 좋아요 버튼 */}
                                <button
                                    className={`${styles.likeBtn} ${question.iLiked ? styles.liked : ''}`}
                                    onClick={e => toggleLike(e, question.id)}
                                >
                                    {/* 이모지 + 숫자 */}
                                    👍 {question.likes}
                                </button>

                                {/* 댓글달기 버튼: 클릭하면 해당 질문의 댓글 입력창 열기/닫기 */}
                                <button
                                    className={styles.commentBtn}
                                    onClick={e => toggleCommentInput(e, question.id)}
                                >
                                    <FiMessageSquare size={13} />
                                    &nbsp;댓글달기
                                </button>
                            </div>
                        </div>

                        {/* ── 댓글 미리보기 (항상 노출, 최대 MAX_VISIBLE_COMMENTS개) ──
                            댓글이 있을 때만 렌더링
                            slice(0, MAX_VISIBLE_COMMENTS)로 앞 3개만 잘라서 표시 */}
                        {question.comments.length > 0 && (
                            <div className={styles.commentPreview}>
                                {question.comments.slice(0, MAX_VISIBLE_COMMENTS).map(comment => (
                                    <div key={comment.id} className={styles.commentItem}>
                                        {/* 작성자 이름 (운영진이면 파란색으로 강조) */}
                                        <span className={`${styles.commentAuthor} ${comment.isStaff ? styles.staffAuthor : ''}`}>
                                            {comment.author}
                                            {comment.isStaff && (
                                                <span className={styles.staffBadge}>🔵</span>
                                            )}
                                        </span>
                                        {/* 댓글 내용 */}
                                        <div className={styles.commentContent}>
                                            ↳ {comment.content}
                                        </div>
                                    </div>
                                ))}

                                {/* 댓글이 3개를 초과하면 "외 N개" 텍스트로 나머지 개수만 안내 */}
                                {/* 상세 페이지에서 전체 댓글을 볼 수 있으므로 더보기 버튼 없이 텍스트만 표시 */}
                                {question.comments.length > MAX_VISIBLE_COMMENTS && (
                                    <span className={styles.commentMore}>
                                        외 {question.comments.length - MAX_VISIBLE_COMMENTS}개 댓글
                                    </span>
                                )}
                            </div>
                        )}

                        {/* ── 댓글 입력창 ──
                            commentOpenId === question.id 일 때만 렌더링
                            (조건이 false면 아무것도 그리지 않아) */}
                        {commentOpenId === question.id && (
                            <div
                                className={styles.commentInputRow}
                                onClick={e => e.stopPropagation()} // 카드 클릭(상세 이동) 방지
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
                                    autoFocus // 입력창 열리면 자동으로 포커스
                                />
                                <button
                                    className={styles.submitBtn}
                                    onClick={e => handleCommentSubmit(e, question.id)}
                                >
                                    ↑
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>


            {/* ── 하단 고정: 새 질문 입력창 ──
                position: fixed로 화면 하단에 고정돼 있어
                스크롤해도 항상 보여 */}
            <div className={styles.newQuestionBar}>

                {/* 에러 메시지: submitError가 있을 때만 보여줌 */}
                {submitError && (
                    <p className={styles.errorMsg}>{submitError}</p>
                )}

                <div className={styles.newQuestionInputRow}>
                    <span className={styles.newQuestionPlus}>+</span>
                    <input
                        className={styles.newQuestionInput}
                        placeholder="질문을 남겨주세요..."
                        value={newQuestion}
                        onChange={e => setNewQuestion(e.target.value)}
                        onKeyDown={e => {
                            if (e.key === 'Enter') handleNewQuestion();
                        }}
                        // 요청 중엔 입력 막기
                        disabled={isSubmitting}
                    />
                    <button
                        className={styles.newQuestionSubmit}
                        onClick={handleNewQuestion}
                        // isSubmitting이 true면 버튼 비활성화 (중복 제출 방지)
                        disabled={isSubmitting}
                    >
                        {/* 요청 중이면 ... 아이콘, 아니면 화살표 */}
                        {isSubmitting ? '⏳' : '↑'}
                    </button>
                </div>
            </div>

        </div>
    );
}

export default QnAListPage;


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// QnAMainPage에서 이 페이지로 이동하는 방법
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━═══
//
// ① React Router 설치:
//    npm install react-router-dom
//
// ② App.jsx (또는 라우터 설정 파일) 에서:
//    import { BrowserRouter, Routes, Route } from 'react-router-dom';
//    import QNAMainPage from './QNAMainPage';
//    import QnADetailPage from './QnADetailPage';
//
//    <BrowserRouter>
//      <Routes>
//        <Route path="/qna" element={<QNAMainPage />} />
//        <Route path="/qna/:sessionId" element={<QnADetailPage />} />
//      </Routes>
//    </BrowserRouter>
//
// ③ QnAMainPage.jsx 카드/리스트 클릭 시:
//    import { useNavigate } from 'react-router-dom';
//    const navigate = useNavigate();
//
//    // 카드 onClick:
//    onClick={() => navigate(`/qna/${session.id}`, { state: { title: `${session.week} 세션(${session.title})` } })}
//
// ④ 이 컴포넌트에서 제목 받아오려면:
//    import { useLocation } from 'react-router-dom';
//    const { state } = useLocation();
//    // sessionTitle prop 대신: state?.title