import { useState, useEffect } from 'react';
import styles from './CurriculumPage.module.css';
import { authFetch } from '../../utils/Api';
import LogoImg from '../../assets/images/logo.png';
import AmImg from '../../assets/images/am.png';
import PmImg from '../../assets/images/pm.png';
import Toggle1 from '../../assets/images/icon_togle1.svg';

const role = localStorage.getItem('role') || 'MEMBER';

const DAY_LABEL = { TUESDAY: '화요일', THURSDAY: '목요일', SATURDAY: '토요일' };
const STATUS_OPTIONS = ['BEFORE', 'ONGOING', 'AFTER'];
const STATUS_LABEL = { BEFORE: '세션 전', ONGOING: '세션 중', AFTER: '세션 후' };

// ── 세션 정보 렌더 (공통) ─────────────────────────────
function SessionInfo({ session, isAdmin }) {
    const icon = session.dayPart === 'AM' ? AmImg : PmImg;
    const label = session.dayPart === 'AM' ? '오전 세션' : '오후 세션';

    return (
        <div className={styles.sessionInfo}>
            <div className={styles.sessionTitleRow}>
                <img src={icon} className={styles.sessionIcon} alt={label} />
                <span className={styles.sessionTitle}>{session.title}</span>
                <span className={styles.sessionHost}>{session.hostName}</span>
            </div>
            <div className={styles.sessionDetailRow}>
                <span className={styles.sessionDetailLabel}>세션 자료</span>
                {session.sessionMaterialUrl
                    ? <a href={session.sessionMaterialUrl} className={styles.sessionLink} target="_blank" rel="noreferrer">{session.sessionMaterialName || '링크'}</a>
                    : <span className={styles.sessionDetailVal}>{session.sessionMaterialName || '-'}</span>
                }
            </div>
            <div className={styles.sessionDetailRow}>
                {session.recordingUrl
                    ? <a href={session.recordingUrl} className={styles.sessionLink} target="_blank" rel="noreferrer">녹화본</a>
                    : <span className={styles.sessionDetailVal}>-</span>
                }
                {session.recordingPassword && <span className={styles.sessionPw}>PW : {session.recordingPassword}</span>}
            </div>
        </div>
    );
}

// ── 부원용 세션 카드 ──────────────────────────────────
function MemberSessionCard({ day }) {
    const [isOpen, setIsOpen] = useState(false);
    const amSession = day.sessions?.find(s => s.dayPart === 'AM');
    const pmSession = day.sessions?.find(s => s.dayPart === 'PM');
    const weekDay = DAY_LABEL[day.dayOfWeek] || '';

    return (
        <div className={styles.sessionCard}>
            <div className={styles.cardHeader} onClick={() => setIsOpen(p => !p)}>
                <div className={styles.cardHeaderLeft}>
                    <span className={styles.cardTitle}>{day.week}주차 {weekDay} 세션</span>
                    <span className={styles.cardDate}>{day.sessionDate}</span>
                </div>
                <img src={Toggle1}  className={`${styles.toggleIcon} ${isOpen ? styles.toggleOpen : ''}`} alt="toggle" />                
            </div>
            <hr className={styles.divider}/>

            {isOpen && (
                <div className={styles.cardBody}>
                    {amSession && <SessionInfo session={amSession} />}
                    {pmSession && <SessionInfo session={pmSession} />}
                    {(day.assignmentName || day.assignmentUrl) && (
                        <div className={styles.assignmentRow}>
                            <span className={styles.assignmentLabel}>과제</span>
                            {day.assignmentUrl
                                ? <a href={day.assignmentUrl} className={styles.sessionLink} target="_blank" rel="noreferrer">{day.assignmentName || '링크'}</a>
                                : <span>{day.assignmentName}</span>
                            }
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

// ── 운영진용 세션 카드 ────────────────────────────────
function AdminSessionCard({ day, onEdit, onDelete }) {
    const [isOpen, setIsOpen] = useState(true);
    const amSession = day.sessions?.find(s => s.dayPart === 'AM');
    const pmSession = day.sessions?.find(s => s.dayPart === 'PM');
    const weekDay = DAY_LABEL[day.dayOfWeek] || '';

    return (
        <div className={styles.sessionCard}>
            <div className={styles.cardHeader} onClick={() => setIsOpen(p => !p)}>
                <div className={styles.cardHeaderLeft}>
                    <span className={styles.cardTitle}>{day.week}주차 {weekDay} 세션</span>
                    <span className={styles.cardDate}>{day.sessionDate}</span>
                </div>
                <img src={Toggle1} className={`${styles.toggleIcon} ${isOpen ? styles.toggleOpen : ''}`} alt="toggle" />
            </div>
            <hr className={styles.divider} /> 

            {isOpen && (
                <div className={styles.cardBody}>
                    {amSession && <SessionInfo session={amSession} isAdmin />}
                    {pmSession && <SessionInfo session={pmSession} isAdmin />}
                    {(day.assignmentName || day.assignmentUrl) && (
                        <div className={styles.assignmentRow}>
                            <span className={styles.assignmentLabel}>과제</span>
                            {day.assignmentUrl
                                ? <a href={day.assignmentUrl} className={styles.sessionLink} target="_blank" rel="noreferrer">{day.assignmentName || '링크'}</a>
                                : <span>{day.assignmentName}</span>
                            }
                        </div>
                    )}
                    <div className={styles.adminBtns}>
                        <button className={styles.editBtn} onClick={() => onEdit(day)}>수정</button>
                        <button className={styles.deleteBtn} onClick={() => onDelete(day.sessionDate)}>삭제</button>
                    </div>
                </div>
            )}
        </div>
    );
}

// ── 운영진 세션 생성/수정 폼 ──────────────────────────
function SessionForm({ day, week, onClose, onSave }) {
    const isEdit = !!day;
    const [form, setForm] = useState({
        week: day?.week || week || 1,
        sessionDate: day?.sessionDate || '',
        generation: day?.generation || 25,
        amTitle: day?.sessions?.find(s => s.dayPart === 'AM')?.title || '',
        amHost: day?.sessions?.find(s => s.dayPart === 'AM')?.hostName || '',
        amMaterialUrl: day?.sessions?.find(s => s.dayPart === 'AM')?.sessionMaterialUrl || '',
        amMaterialName: day?.sessions?.find(s => s.dayPart === 'AM')?.sessionMaterialName || '',
        amRecordingUrl: day?.sessions?.find(s => s.dayPart === 'AM')?.recordingUrl || '',
        amRecordingPw: day?.sessions?.find(s => s.dayPart === 'AM')?.recordingPassword || '',
        amStatus: day?.sessions?.find(s => s.dayPart === 'AM')?.status || 'BEFORE',
        pmTitle: day?.sessions?.find(s => s.dayPart === 'PM')?.title || '',
        pmHost: day?.sessions?.find(s => s.dayPart === 'PM')?.hostName || '',
        pmMaterialUrl: day?.sessions?.find(s => s.dayPart === 'PM')?.sessionMaterialUrl || '',
        pmMaterialName: day?.sessions?.find(s => s.dayPart === 'PM')?.sessionMaterialName || '',
        pmRecordingUrl: day?.sessions?.find(s => s.dayPart === 'PM')?.recordingUrl || '',
        pmRecordingPw: day?.sessions?.find(s => s.dayPart === 'PM')?.recordingPassword || '',
        pmStatus: day?.sessions?.find(s => s.dayPart === 'PM')?.status || 'BEFORE',
        assignmentUrl: day?.assignmentUrl || '',
        assignmentName: day?.assignmentName || '',
    });

    // sessionDate 변경 시 요일 자동 계산
    const getWeekDay = (dateStr) => {
        if (!dateStr) return '';
        const [year, month, day] = dateStr.split('-').map(Number);
        const date = new Date(year, month - 1, day);
        const map = { 2: '화요일', 4: '목요일', 6: '토요일' };
        return map[date.getDay()] || '';
    };   

    const handleSave = async () => {
        const body = {
            generation: Number(form.generation),
            week: Number(form.week),
            sessionDate: form.sessionDate,
            sessions: [
                {
                    dayPart: 'AM',
                    title: form.amTitle,
                    hostName: form.amHost,
                    sessionMaterialUrl: form.amMaterialUrl,
                    sessionMaterialName: form.amMaterialName,
                    recordingUrl: form.amRecordingUrl,
                    recordingPassword: form.amRecordingPw,
                    status: form.amStatus,
                },
                {
                    dayPart: 'PM',
                    title: form.pmTitle,
                    hostName: form.pmHost,
                    sessionMaterialUrl: form.pmMaterialUrl,
                    sessionMaterialName: form.pmMaterialName,
                    recordingUrl: form.pmRecordingUrl,
                    recordingPassword: form.pmRecordingPw,
                    assignmentUrl: form.assignmentUrl,
                    assignmentName: form.assignmentName,
                    status: form.pmStatus,
                },
            ],
        };

        if (isEdit) {
            await authFetch(`/api/curriculums/${day.sessionDate}`, {
                method: 'PATCH',
                body: JSON.stringify({
                    generation: body.generation,
                    week: body.week,
                    newSessionDate: form.sessionDate,
                    sessions: body.sessions,
                }),
            });
        } else {
            await authFetch('/api/curriculums', {
                method: 'POST',
                body: JSON.stringify(body),
            });
        }
        onSave();
        onClose();
    };

    const weeks = [1, 2, 3, 4, 5];

    return (
        <div className={styles.formOverlay}>
            <div className={styles.formCard}>
              <div className={styles.formSection}>
                  <label className={styles.formLabel}>주차</label>
                  <select className={styles.formInput} value={form.week}
                      onChange={e => setForm({ ...form, week: e.target.value })}>
                      {weeks.map(w => <option key={w} value={w}>{w}주차</option>)}
                  </select>
              </div>

              <div className={styles.formRow2}>
                  <div className={styles.formSection}>
                      <label className={styles.formLabel}>제목</label>
                      <input className={styles.formInput} 
                          value={`${form.week}주차 ${getWeekDay(form.sessionDate)} 세션`}
                          readOnly />
                  </div>
                  <div className={styles.formSection}>
                      <label className={styles.formLabel}>날짜</label>
                      <input className={styles.formInput} type="date" value={form.sessionDate}
                          onChange={e => setForm({ ...form, sessionDate: e.target.value })} />
                  </div>
              </div>

                {/* 오전 세션 */}
                <div className={styles.formSectionTitle}>
                    <img src={AmImg} className={styles.sessionIcon} alt="AM" />
                    <span className={styles.amLabel}>오전 세션</span>
                    <div className={styles.statusBtns}>
                        {STATUS_OPTIONS.map(s => (
                            <button key={s}
                                className={`${styles.statusBtn} ${form.amStatus === s ? styles.statusActive : ''}`}
                                onClick={() => setForm({ ...form, amStatus: s })}>
                                {STATUS_LABEL[s]}
                            </button>
                        ))}
                    </div>
                </div>
                <div className={styles.formGrid}>
                    <div><label className={styles.formLabel}>세션 제목</label><input className={styles.formInput} value={form.amTitle} onChange={e => setForm({ ...form, amTitle: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션자</label><input className={styles.formInput} value={form.amHost} onChange={e => setForm({ ...form, amHost: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션 자료</label><input className={styles.formInput} value={form.amMaterialName} onChange={e => setForm({ ...form, amMaterialName: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션 자료 링크</label><input className={styles.formInput} value={form.amMaterialUrl} onChange={e => setForm({ ...form, amMaterialUrl: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>녹화본 링크</label><input className={styles.formInput} value={form.amRecordingUrl} onChange={e => setForm({ ...form, amRecordingUrl: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>녹화본 비밀번호</label><input className={styles.formInput} value={form.amRecordingPw} onChange={e => setForm({ ...form, amRecordingPw: e.target.value })} /></div>
                </div>

                {/* 오후 세션 */}
                <div className={styles.formSectionTitle}>
                    <img src={PmImg} className={styles.sessionIcon} alt="PM" />
                    <span className={styles.pmLabel}>오후 세션</span>
                    <div className={styles.statusBtns}>
                        {STATUS_OPTIONS.map(s => (
                            <button key={s}
                                className={`${styles.statusBtn} ${form.pmStatus === s ? styles.statusActive : ''}`}
                                onClick={() => setForm({ ...form, pmStatus: s })}>
                                {STATUS_LABEL[s]}
                            </button>
                        ))}
                    </div>
                </div>
                <div className={styles.formGrid}>
                    <div><label className={styles.formLabel}>세션 제목</label><input className={styles.formInput} value={form.pmTitle} onChange={e => setForm({ ...form, pmTitle: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션자</label><input className={styles.formInput} value={form.pmHost} onChange={e => setForm({ ...form, pmHost: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션 자료</label><input className={styles.formInput} value={form.pmMaterialName} onChange={e => setForm({ ...form, pmMaterialName: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>세션 자료 링크</label><input className={styles.formInput} value={form.pmMaterialUrl} onChange={e => setForm({ ...form, pmMaterialUrl: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>녹화본 링크</label><input className={styles.formInput} value={form.pmRecordingUrl} onChange={e => setForm({ ...form, pmRecordingUrl: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>녹화본 비밀번호</label><input className={styles.formInput} value={form.pmRecordingPw} onChange={e => setForm({ ...form, pmRecordingPw: e.target.value })} /></div>
                </div>

                {/* 과제 */}
                <div className={styles.assignmentSection}>
                    <span className={styles.assignmentLabel}>과제</span>
                    <div><label className={styles.formLabel}>과제 제목</label><input className={styles.formInput} style={{width:'100%'}} value={form.assignmentName} onChange={e => setForm({ ...form, assignmentName: e.target.value })} /></div>
                    <div><label className={styles.formLabel}>과제 링크</label><input className={styles.formInput} style={{width:'100%'}} value={form.assignmentUrl} onChange={e => setForm({ ...form, assignmentUrl: e.target.value })} /></div>
                </div>

                <button className={styles.saveFormBtn} onClick={handleSave}>저장하기</button>
                <button className={styles.cancelBtn} onClick={onClose}>취소</button>
            </div>
        </div>
    );
}

// ── 메인 컴포넌트 ─────────────────────────────────────
function CurriculumPage() {
    const [days, setDays] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [editDay, setEditDay] = useState(null);
    const [createWeek, setCreateWeek] = useState(null);

    const fetchDays = async () => {
        try {
            const res = await authFetch('/api/curriculums');
            const data = await res.json();
            setDays(Array.isArray(data) ? data : []);
        } catch (e) {}
    };

    useEffect(() => { fetchDays(); }, []);

    const handleDelete = async (sessionDate) => {
        if (!window.confirm('삭제하시겠습니까?')) return;
        await authFetch(`/api/curriculums/${sessionDate}`, { method: 'DELETE' });
        fetchDays();
    };

    // 주차별로 그룹화
    const grouped = days.reduce((acc, day) => {
        const week = day.week;
        if (!acc[week]) acc[week] = [];
        acc[week].push(day);
        return acc;
    }, {});

    return (
        <div className={styles.container}>
            {role === 'ADMIN' && (
                <div className={styles.topBar}>
                    <button className={styles.createBtn} onClick={() => {
                        setEditDay(null);
                        setShowForm(true);
                    }}>
                        세션 생성
                    </button>
                </div>
            )}          
            {Object.entries(grouped).map(([week, weekDays]) => (
                <div key={week} className={styles.weekSection}>
                    <div className={styles.weekHeader}>
                        <div className={styles.weekLeft}>
                            <img src={LogoImg} className={styles.logoIcon} alt="logo" />
                            <span className={styles.weekTitle}>WEEK {week}</span>
                        </div>
                    </div>

                    <div className={styles.cardsRow}>
                        {weekDays.map((day, i) => (
                            role === 'ADMIN'
                                ? <AdminSessionCard key={i} day={day}
                                    onEdit={(d) => { setEditDay(d); setCreateWeek(null); setShowForm(true); }}
                                    onDelete={handleDelete} />
                                : <MemberSessionCard key={i} day={day} />
                        ))}
                    </div>
                </div>
            ))}

            {showForm && (
              
                <SessionForm                
                    day={editDay}
                    week={createWeek}
                    onClose={() => { setShowForm(false); setEditDay(null); setCreateWeek(null); }}
                    onSave={fetchDays}
                />
            )}
        </div>
    );
}

export default CurriculumPage;