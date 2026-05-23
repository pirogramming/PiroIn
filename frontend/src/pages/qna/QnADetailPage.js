import '../../assets/styles/global.css';
import { useState } from 'react';
import styles from './QnADetailPage.module.css';
import { FiChevronLeft, FiMoreVertical, FiCornerDownRight } from 'react-icons/fi';
import {
    CommentImoji,
    MeCuriousToo,
    StaffCheck,
    SumitBtn,
} from '../../components/qna_svg';
import profileImg from '../../assets/images/profile.png';



// ── 목업 데이터 ──────────────────────────────────────────
const MOCK_QUESTION = {
    id: 2,
    author: '익명',
    isStaff: false,
    avatarUrl: null,
    date: '2025/04/25 13:20',
    text: '오류 났어요',
    image: 'https://dora-guide.com/wp-content/uploads/2019/11/Visual-studio-code-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%82%AC%EC%9A%A9%EB%B2%95.png',
    likes: 7,
    iLiked: false,
    isSolved: true,
    comments: [
        {
            id: 1,
            author: '운영진1',
            isStaff: true,
            avatarUrl: null,
            date: '2025/04/25 13:28',
            content: '사진 참고하세요',
            image: 'https://dora-guide.com/wp-content/uploads/2019/11/Visual-studio-code-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%82%AC%EC%9A%A9%EB%B2%95.png',
        },
        {
            id: 2,
            author: '작성자',
            isStaff: false,
            avatarUrl: null,
            date: '2025/04/25 13:28',
            content: '감사합니다',
            image: null,
        },
        {
            id: 3,
            author: '익명1',
            isStaff: false,
            avatarUrl: null,
            date: '2025/04/25 13:28',
            content: '감사합니다',
            image: null,
        },
    ],
};


// ── 메인 컴포넌트 ────────────────────────────────────────
function QnADetailPage({
    question: initialQuestion = MOCK_QUESTION,
    isStaff = false,
    onBack,
}) {
    const [question, setQuestion] = useState(initialQuestion);
    const [commentText, setCommentText] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 좋아요 토글
    const toggleLike = () => {
        setQuestion(prev => ({
            ...prev,
            iLiked: !prev.iLiked,
            likes: prev.iLiked ? prev.likes - 1 : prev.likes + 1,
        }));
    };

    // 댓글 제출
    const handleCommentSubmit = async () => {
        const text = commentText.trim();
        if (!text) return;

        setIsSubmitting(true);
        try {
            const newComment = {
                id: Date.now(),
                author: isStaff ? '운영진' : '나',
                isStaff,
                avatarUrl: null,
                date: new Date().toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                }).replace(/\. /g, '/').replace('.', ''),
                content: text,
                image: null,
            };
            setQuestion(prev => ({
                ...prev,
                comments: [...prev.comments, newComment],
            }));
            setCommentText('');
        } catch (err) {
            console.error('댓글 등록 실패:', err);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className={styles.page}>

            {/* ── 상단 바:해결 여부 ── */}
            <div className={styles.topBar}>
                {question.isSolved ? (
                    <span className={styles.solvedBadge}>해결 질문</span>
                ) : (
                    <span className={styles.unsolvedBadge}>미해결 질문</span>
                )}
            </div>

            {/* ── 작성자 행 ── */}
            <div className={styles.authorRow}>
                <div className={styles.avatar}>
                    <img src={profileImg} alt={question.author} className={styles.avatarImg} />
                </div>
                <div className={styles.authorInfo}>
                    <span className={styles.authorName}>
                        {question.author}
                        {question.isStaff && (
                            <span className={styles.staffBadge}><StaffCheck /></span>
                        )}
                    </span>
                    <span className={styles.authorDate}>{question.date}</span>
                </div>
                <button className={styles.menuBtn} aria-label="더보기">
                    <FiMoreVertical size={20} />
                </button>
            </div>

            {/* ── 질문 제목 ── */}
            <div className={styles.questionTitle}>
                <span className={styles.qIcon}>Q.</span>
                <span className={styles.questionText}>{question.text}</span>
            </div>

            {/* ── 첨부 이미지 ── */}
            {question.image && (
                <img
                    src={question.image}
                    alt="첨부 이미지"
                    className={styles.questionImage}
                />
            )}

            {/* ── 액션 버튼 (저도 궁금해요 / 댓글달기) ── */}
            <div className={styles.actionRow}>
                <button
                    className={`${styles.likeBtn} ${question.iLiked ? styles.liked : ''}`}
                    onClick={toggleLike}
                >
                    <MeCuriousToo /> 저도 궁금해요&nbsp;{question.likes}
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
                {question.comments.map(comment => (
                    <div key={comment.id} className={styles.commentBlock}>

                        {/* 댓글 작성자 */}
                        <div className={styles.commentAuthorRow}>
                            <div className={styles.commentAvatar}>
                                <img src={profileImg} alt={comment.author} className={styles.commentAvatarImg} />
                            </div>
                            <span className={styles.commentAuthorName}>
                                {comment.author}
                                {comment.isStaff && (
                                    <span className={styles.staffBadge}><StaffCheck /></span>
                                )}
                            </span>
                        </div>

                        {/* 댓글 말풍선 */}
                        <div className={styles.commentBubble}>
                            <div className={styles.commentContent}>
                                <FiCornerDownRight size={14} className={styles.commentArrow} />
                                {comment.content}
                            </div>
                            {comment.image && (
                                <img
                                    src={comment.image}
                                    alt="댓글 첨부 이미지"
                                    className={styles.commentImage}
                                />
                            )}
                        </div>

                        {/* 타임스탬프 */}
                        <p className={styles.commentDate}>{comment.date}</p>
                    </div>
                ))}
            </div>

            {/* ── 하단 그라디언트 커버 ── */}
            <div className={styles.bottomCover} />

            {/* ── 댓글 입력 바 (하단 고정) ── */}
            <div className={styles.commentInputBar}>
                <input
                    id="commentInput"
                    className={styles.commentInput}
                    placeholder="댓글을 입력해주세요..."
                    value={commentText}
                    onChange={e => setCommentText(e.target.value)}
                    onKeyDown={e => {
                        if (e.key === 'Enter') handleCommentSubmit();
                    }}
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