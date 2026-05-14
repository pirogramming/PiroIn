import { useState, useEffect } from 'react';
import styles from './CurriculumPage.module.css';
import weekinfo from '../../assets/images/week.png';
import amimg from '../../assets/images/am.png';
import pmimg from '../../assets/images/pm.png';

function getDayLabel(dateStr) {
  const day = new Date(dateStr).getDay();
  const map = { 2: '화요일', 4: '목요일', 6: '토요일' };
  return map[day] || '';
}

function SessionCard({ date, sessions }) {
  const [open, setOpen] = useState(false);
  const amSession = sessions.find(s => s.dayPart === 'AM');
  const pmSession = sessions.find(s => s.dayPart === 'PM');
  const hasDetail = sessions.some(s => s.title || s.hostName);
  const week = sessions[0].week;

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader} onClick={() => hasDetail && setOpen(!open)}>
        <div className={styles.cardTitleWrap}>
          <span className={styles.cardTitle}>
            {week}주차 {getDayLabel(date)} 세션
          </span>
          <span className={styles.cardDate}>{date}</span>
        </div>
        {hasDetail && (
          <span className={styles.toggle}>{open ? '▲' : '▼'}</span>
        )}
      </div>

      <div className={styles.divider} />

      {/* 토글 닫혔을 때 - 제목만 보임 */}
      {!open && hasDetail && (
        <div className={styles.cardBody}>
          {amSession && amSession.title && (
            <div className={styles.sectionRow}>
              <span className={styles.sectionTitle}>
                <img src={amimg} alt="오전" className={styles.amIcon} /> {amSession.title}
              </span>
            </div>
          )}
          {pmSession && pmSession.title && (
            <div className={styles.sectionRow}>
              <span className={styles.sectionTitle}>
                <img src={pmimg} alt="오후" className={styles.pmIcon} /> {pmSession.title}
              </span>
            </div>
          )}
        </div>
      )}

      {/* 토글 열렸을 때 - 전체 보임 */}
      {open && hasDetail && (
        <div className={styles.cardBody}>
          {amSession && amSession.title && (
            <div className={styles.section}>
              <div className={styles.sectionRow}>
                <span className={styles.sectionTitle}>
                  <img src={amimg} alt="오전" className={styles.amIcon} /> {amSession.title}
                </span>
                <span className={styles.host}>{amSession.hostName}</span>
              </div>
              <div className={styles.sectionRow}>
                <a href={amSession.sessionMaterialUrl} className={styles.file_row} target="_blank" rel="noreferrer">
                  <span className={styles.file}>세션 자료</span>
                  <span className={styles.file_name}>{amSession.sessionMaterialName}</span>
                </a>
              </div>
              <div className={styles.sectionRow}>
                <a href={amSession.recordingUrl} className={styles.video_row} target="_blank" rel="noreferrer">
                  <span className={styles.video}>녹화본</span>
                  <span className={styles.video_pw}>
                    {amSession.recordingPassword ? `PW : ${amSession.recordingPassword}` : '아직 공개되지 않았습니다.'}
                  </span>
                </a>
              </div>
            </div>
          )}
          {pmSession && pmSession.title && (
            <div className={styles.section}>
              <div className={styles.sectionRow}>
                <span className={styles.sectionTitle}>
                  <img src={pmimg} alt="오후" className={styles.pmIcon} /> {pmSession.title}
                </span>
                <span className={styles.host}>{pmSession.hostName}</span>
              </div>
              <div className={styles.sectionRow}>
                <a href={pmSession.sessionMaterialUrl} className={styles.file_row} target="_blank" rel="noreferrer">
                  <span className={styles.file}>세션 자료</span>
                  <span className={styles.file_name}>{pmSession.sessionMaterialName}</span>
                </a>
              </div>
              <div className={styles.sectionRow}>
                <a href={pmSession.recordingUrl} className={styles.video_row} target="_blank" rel="noreferrer">
                  <span className={styles.video}>녹화본</span>
                  <span className={styles.video_pw}>
                    {pmSession.recordingPassword ? `PW : ${pmSession.recordingPassword}` : '아직 공개되지 않았습니다.'}
                  </span>
                </a>
              </div>
            </div>
          )}

          <div className={styles.section}>
            <span className={styles.assignmentLabel}>과제</span>
            <p className={styles.assignmentText}>
              {amSession?.description || '아직 공개되지 않았습니다.'}
            </p>
          </div>
        </div>
      )}

      {!hasDetail && (
        <div className={styles.cardBody}>
          <p className={styles.placeholder}>세션 정보가 아직 등록되지 않았습니다.</p>
        </div>
      )}
    </div>
  );
}

function WeekSection({ week, dateGroup }) {
  return (
    <div className={styles.weekSection}>
      <div className={styles.weekHeader}>
        <div className={styles.weekLogo}>
          <img src={weekinfo} alt="로고" />
        </div>
        <h2 className={styles.weekTitle}>WEEK {week}</h2>
      </div>
      <div className={styles.cardGrid}>
        {Object.entries(dateGroup).map(([date, sessions]) => (
          <SessionCard key={date} date={date} sessions={sessions} />
        ))}
      </div>
    </div>
  );
}

function CurriculumPage() {
  const [sessions, setSessions] = useState([]);
  const role = localStorage.getItem('role');

  useEffect(() => {
    setSessions([
      {
        id: 1,
        week: 1,
        sessionDate: '2026-06-23',
        dayPart: 'AM',
        title: 'HTML/CSS',
        hostName: '24기 김서윤',
        status: 'AFTER_SESSION',
        description: '코딩앵무 클론 코딩, 피로그래밍 페이지 클론 코딩',
        sessionMaterialUrl: '#',
        sessionMaterialName: 'HTML/CSS',
        recordingUrl: '#',
        recordingPassword: '%8.D^G&z',
      },
      {
        id: 2,
        week: 1,
        sessionDate: '2026-06-23',
        dayPart: 'PM',
        title: 'Git 기초',
        hostName: '24기 한혜담',
        status: 'AFTER_SESSION',
        description: '코딩앵무 클론 코딩, 피로그래밍 페이지 클론 코딩',
        sessionMaterialUrl: '#',
        sessionMaterialName: 'Git 기초',
        recordingUrl: '#',
        recordingPassword: '%8.D^G&z',
      },
      {
        id: 3,
        week: 1,
        sessionDate: '2026-06-25',
        dayPart: 'AM',
        title: '',
        hostName: '',
        status: 'BEFORE_SESSION',
        description: '',
        sessionMaterialUrl: '',
        sessionMaterialName: '',
        recordingUrl: '',
        recordingPassword: '',
      },
      {
        id: 4,
        week: 1,
        sessionDate: '2026-06-27',
        dayPart: 'AM',
        title: '',
        hostName: '',
        status: 'BEFORE_SESSION',
        description: '',
        sessionMaterialUrl: '',
        sessionMaterialName: '',
        recordingUrl: '',
        recordingPassword: '',
      },
    ]);
  }, []);

  const grouped = sessions.reduce((acc, session) => {
    const week = session.week;
    const date = session.sessionDate;
    if (!acc[week]) acc[week] = {};
    if (!acc[week][date]) acc[week][date] = [];
    acc[week][date].push(session);
    return acc;
  }, {});

  return (
    <div className={styles.page}>
      {Object.entries(grouped).map(([week, dateGroup]) => (
        <WeekSection key={week} week={week} dateGroup={dateGroup} />
      ))}
    </div>
  );
}

export default CurriculumPage;