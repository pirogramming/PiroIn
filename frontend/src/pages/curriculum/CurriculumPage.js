import { useState, useEffect } from 'react';
import styles from './CurriculumPage.module.css';

const DAY_LABEL = { 1: '화요일', 2: '목요일', 3: '토요일' };

function getDayLabel(dateStr) {
  const day = new Date(dateStr).getDay();
  const map = { 2: '화요일', 4: '목요일', 6: '토요일' };
  return map[day] || '';
}

function SessionCard({ session }) {
  const [open, setOpen] = useState(false);
  const hasDetail = session.title || session.hostName;

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader} onClick={() => hasDetail && setOpen(!open)}>
        <div className={styles.cardTitleWrap}>
          <span className={styles.cardTitle}>
            {session.week}주차 {getDayLabel(session.sessionDate)} 세션
          </span>
          <span className={styles.cardDate}>{session.sessionDate}</span>
        </div>
        {hasDetail && (
          <span className={styles.toggle}>{open ? '▲' : '▼'}</span>
        )}
      </div>

      {open && hasDetail && (
        <div className={styles.cardBody}>
          <div className={styles.section}>
            <div className={styles.sectionRow}>
              <span className={styles.sectionTitle}>✳ {session.title}</span>
              <span className={styles.host}>{session.hostName}</span>
            </div>
            <div className={styles.sectionRow}>
              <a href={session.sessionMaterialUrl} className={styles.link} target="_blank" rel="noreferrer">
                세션 자료
              </a>
              <span className={styles.value}>{session.sessionMaterialName}</span>
            </div>
            <div className={styles.sectionRow}>
              <span className={styles.label}>녹화본</span>
              <span className={styles.value}>
                {session.recordingPassword ? `PW : ${session.recordingPassword}` : '아직 공개되지 않았습니다.'}
              </span>
            </div>
          </div>

          <div className={styles.divider} />

          <div className={styles.section}>
            <span className={styles.assignmentLabel}>과제</span>
            <p className={styles.assignmentText}>
              {session.description || '아직 공개되지 않았습니다.'}
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

function WeekSection({ week, sessions }) {
  return (
    <div className={styles.weekSection}>
      <div className={styles.weekHeader}>
        <div className={styles.weekLogo}>
          <div className={styles.weekLogoCircle} />
          <div className={styles.weekLogoCircle} />
          <div className={styles.weekLogoCircle} />
          <div className={styles.weekLogoCircle} />
        </div>
        <h2 className={styles.weekTitle}>WEEK {week}</h2>
      </div>
      <div className={styles.cardGrid}>
        {sessions.map((session) => (
          <SessionCard key={session.id} session={session} />
        ))}
      </div>
    </div>
  );
}

function CurriculumPage() {
  const [sessions, setSessions] = useState([]);
  const role = localStorage.getItem('role');
  useEffect(() => {
  // 임시 데이터 (API 연결 전 UI 확인용)
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
      assignmentUrl: '#',
      assignmentName: '과제1',
    },
    {
      id: 2,
      week: 1,
      sessionDate: '2026-06-25',
      dayPart: 'AM',
      title: 'js 기본 & js dom',
      hostName: '',
      status: 'BEFORE_SESSION',
      description: '',
      sessionMaterialUrl: '',
      sessionMaterialName: '',
      recordingUrl: '',
      recordingPassword: '',
      assignmentUrl: '',
      assignmentName: '',
    },
    {
      id: 3,
      week: 1,
      sessionDate: '2026-06-27',
      dayPart: 'AM',
      title: 'DB 개론',
      hostName: '',
      status: 'BEFORE_SESSION',
      description: '',
      sessionMaterialUrl: '',
      sessionMaterialName: '',
      recordingUrl: '',
      recordingPassword: '',
      assignmentUrl: '',
      assignmentName: '',
    },
  ]);
}, []);

  // useEffect(() => {
  //   fetch('/api/curriculums', {
  //     headers: { 'Content-Type': 'application/json' },
  //   })
  //     .then((res) => res.json())
  //     .then((data) => setSessions(data))
  //     .catch(() => console.error('커리큘럼 불러오기 실패'));
  // }, []);

  // week 단위로 그룹핑
  const grouped = sessions.reduce((acc, session) => {
    const week = session.week;
    if (!acc[week]) acc[week] = [];
    acc[week].push(session);
    return acc;
  }, {});

  return (
    <div className={styles.page}>
      {Object.entries(grouped).map(([week, sessions]) => (
        <WeekSection key={week} week={week} sessions={sessions} />
      ))}
    </div>
  );
}

export default CurriculumPage;