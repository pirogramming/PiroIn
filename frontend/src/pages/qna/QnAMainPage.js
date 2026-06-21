import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './QnAMainPage.module.css';
import { FiLogIn } from 'react-icons/fi';
import { authFetch } from '../../utils/Api';
import { getIcon, getTime, formatDate, DAY_OF_WEEK_KO, DAY_PART_KO } from '../../utils/qnaUtils';


function QNAMainPage() {
    const navigate = useNavigate();

    // ── 세션 목록 상태 ──────────────────────────────
    const [activeSessions, setActiveSessions] = useState([]);
    const [pastSessions, setPastSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // ── 세션 목록 불러오기 ──────────────────────────
    useEffect(() => {
        document.title = "Q&A | PIROIN";

        const fetchSessions = async () => {
            try {
                setLoading(true);
                const res = await authFetch('/api/sessions');
                if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
                const json = await res.json();
                if (!json.isSuccess) throw new Error(json.message);

                setActiveSessions(json.result.activeSessions ?? []);
                setPastSessions(json.result.pastSessions ?? []);
            } catch (err) {
                console.error('세션 불러오기 실패:', err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchSessions();
    }, []);

    if (loading) return <div className={styles.page}>불러오는 중...</div>;
    if (error) return <div className={styles.page}>오류: {error}</div>;

    return (
        
            <div className={styles.page}>

                {/* ── 진행 중인 세션 ── */}
                {activeSessions.length > 0 && (
                    <>
                        <section className={styles.section}>
                            <h2 className={styles.sectionTitle}>현재 세션</h2>
                            {activeSessions.map(session => (
                                <div
                                    key={session.sessionId}
                                    className={styles.card}
                                    onClick={() => navigate(`/sessions/${session.sessionId}/questions`, { state: { status: 'IN_SESSION' } })}
                                >
                                    <p className={styles.cardTitle}>
                                        <span className={styles.icon}>{getIcon(session.dayPart)}</span>
                                        {session.title}
                                    </p>
                                    <p className={styles.cardWeek}>
                                        {session.week}주차 {DAY_OF_WEEK_KO[session.dayOfWeek]} {DAY_PART_KO[session.dayPart]}
                                    </p>
                                    <p className={styles.cardDate}>{formatDate(session.sessionDate)}</p>
                                    <p className={styles.cardTime}>{getTime(session.dayPart)}</p>
                                </div>
                            ))}
                        </section>
                        <hr className={styles.divider} />
                    </>
                )}

                {/* ── 지난 세션 ── */}
                {pastSessions.length > 0 && (
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>지난 세션</h2>
                        <div className={styles.list}>
                            {pastSessions.map(session => (
                                <div
                                    key={session.sessionId}
                                    className={styles.listItem}
                                    onClick={() => navigate(`/sessions/${session.sessionId}/questions`, { state: { status: 'AFTER_SESSION' } })}
                                >
                                    <span>
                                        <span className={styles.icon}>{getIcon(session.dayPart)}</span>
                                        <span className={styles.listTitle}>{session.title}</span>
                                        <span className={styles.listWeek}>
                                            &nbsp;• {session.week}주차 {DAY_OF_WEEK_KO[session.dayOfWeek]} {DAY_PART_KO[session.dayPart]}
                                        </span>
                                    </span>
                                    <button className={styles.enterBtn}>
                                        <FiLogIn className={styles.enterIcon} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {/* ── 세션 없을 때 ── */}
                {activeSessions.length === 0 && pastSessions.length === 0 && (
                    <section className={styles.section}>
                        <p className={styles.empty}>아직 생성된 Q&A가 없어요</p>
                    </section>
                )}

            </div>
        
    );
}

export default QNAMainPage;