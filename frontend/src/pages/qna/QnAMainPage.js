import styles from './QnAMainPage.module.css';
import { FiLogIn } from 'react-icons/fi';

const nowSessions = [ // css 보려고 걍 적어둠
    { id: 1, icon: '☀', title: 'HTML/CSS', week: '1주차 화요일 오전', date: '2026.04.19', time: '10:00 ~ 13:00' }
];

const pastSessions = [ // css 보려고 걍 적어둠
    { id: 1, icon: '☀', title: 'HTML/CSS', week: '1주차 화요일 오전' },
    { id: 2, icon: '☾', title: 'Git 기초', week: '1주차 화요일 오후' },
    { id: 3, icon: '☀', title: 'HTML/CSS', week: '1주차 화요일 오전' },
    { id: 4, icon: '☾', title: 'Git 기초', week: '1주차 화요일 오후' },
    { id: 5, icon: '☀', title: 'HTML/CSS', week: '1주차 화요일 오전' },
    { id: 6, icon: '☾', title: 'Git 기초', week: '1주차 화요일 오후' },
];

function QNAMainPage() {

    return (
        <div className={styles.page}>
            <section className={styles.section}>
                <h2 className={styles.sectionTitle}>Q&A</h2>
                {nowSessions.map(session => (
                    <div key={session.id} className={styles.card}>
                        <p className={styles.cardTitle}>
                            <span className={styles.icon}>{session.icon}</span>
                            {session.title}
                        </p>
                        <p className={styles.cardWeek}>{session.week}</p>
                        <p className={styles.cardDate}>{session.date}</p>
                        <p className={styles.cardTime}>{session.time}</p>
                    </div>
                ))}
            </section>

            <hr className={styles.divider} />

            <section className={styles.section}>
                <h2 className={styles.sectionTitle}>지난 세션</h2>
                <div className={styles.list}>
                    {pastSessions.map(session => (
                        <div key={session.id} className={styles.listItem}>
                            <span>
                                <span className={styles.icon}>{session.icon}</span>
                                <span className={styles.listTitle}>{session.title}</span>
                                <span className={styles.listWeek}> •{session.week}</span>
                            </span>
                            <button className={styles.enterBtn}><FiLogIn size={25} /></button>
                        </div>
                    ))}
                </div>
            </section>

        </div>
    );
}

export default QNAMainPage;