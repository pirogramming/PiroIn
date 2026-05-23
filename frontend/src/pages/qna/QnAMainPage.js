import { useState, useEffect } from 'react';
import styles from './QnAMainPage.module.css';
import { FiLogIn } from 'react-icons/fi';

// ─────────────────────────────────────────────
// 📌 목업 데이터 
// ─────────────────────────────────────────────
// const MOCK_DATA = {
//     activeSessions: [
//         { sessionId: 1, week: 1, dayOfWeek: '화요일', dayPart: '오전', sessionDate: '2026.05.23', title: 'HTML/CSS' }
//     ],
//     pastSessions: [
//         { sessionId: 1, week: 1, dayOfWeek: '화요일', dayPart: '오전', sessionDate: '2026.05.23', title: 'HTML/CSS' },
//         { sessionId: 2, week: 1, dayOfWeek: '화요일', dayPart: '오후', sessionDate: '2026.05.23', title: 'Git 기초' },
//     ]
// };
// ─────────────────────────────────────────────

const BASE_URL = '';

const getIcon = (dayPart) => dayPart === '오전' ? '☀' : '☾';
const getTime = (dayPart) => dayPart === '오전' ? '10:00 ~ 13:00' : '14:00 ~ 17:00';

function QNAMainPage() {
    const [activeSessions, setActiveSessions] = useState([]);
    const [pastSessions, setPastSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchSessions = async () => {
            try {
                setLoading(true);

                const token = localStorage.getItem('token');

                const res = await fetch(`${BASE_URL}/api/sessions`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    }
                });

                if (!res.ok) throw new Error(`서버 오류: ${res.status}`);

                const json = await res.json();

                if (!json.isSuccess) throw new Error(json.message);

                // DB에 데이터 없으면 목업으로 대체
                // setActiveSessions(json.result.activeSessions.length > 0 ? json.result.activeSessions : MOCK_DATA.activeSessions);
                // setPastSessions(json.result.pastSessions.length > 0 ? json.result.pastSessions : MOCK_DATA.pastSessions);

            } catch (err) {
                console.error('세션 불러오기 실패:', err);
                // setActiveSessions(MOCK_DATA.activeSessions);
                // setPastSessions(MOCK_DATA.pastSessions);
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

            {/* 진행 중인 세션 있을 때만 표시 */}
            {activeSessions.length > 0 && (
                <>
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>Q&A</h2>
                        {activeSessions.map(session => (
                            <div key={session.sessionId} className={styles.card}>
                                <p className={styles.cardTitle}>
                                    <span className={styles.icon}>{getIcon(session.dayPart)}</span>
                                    {session.title}
                                </p>
                                <p className={styles.cardWeek}>{session.week}주차 {session.dayOfWeek} {session.dayPart}</p>
                                <p className={styles.cardDate}>{session.sessionDate}</p>
                                <p className={styles.cardTime}>{getTime(session.dayPart)}</p>
                            </div>
                        ))}
                    </section>
                    <hr className={styles.divider} />
                </>
            )}

            {/* 지난 세션 */}
            {pastSessions.length > 0 ? (
                <section className={styles.section}>
                    <h2 className={styles.sectionTitle}>지난 세션</h2>
                    <div className={styles.list}>
                        {pastSessions.map(session => (
                            <div key={session.sessionId} className={styles.listItem}>
                                <span>
                                    <span className={styles.icon}>{getIcon(session.dayPart)}</span>
                                    <span className={styles.listTitle}>{session.title}</span>
                                    <span className={styles.listWeek}> •{session.week}주차 {session.dayOfWeek} {session.dayPart}</span>
                                </span>
                                <button className={styles.enterBtn}><FiLogIn size={25} /></button>
                            </div>
                        ))}
                    </div>
                </section>
            ) : (
                /* 진행 중인 세션도 없고 지난 세션도 없을 때 */
                activeSessions.length === 0 && (
                    <section className={styles.section}>
                        <p className={styles.empty}>아직 생성된 Q&A가 없어요</p>
                    </section>
                )
            )}

        </div>
    );
}

export default QNAMainPage;