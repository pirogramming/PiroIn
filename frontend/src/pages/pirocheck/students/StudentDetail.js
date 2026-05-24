import { useState, useEffect } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import styles from './StudentDetail.module.css';
import ProfileImg from '../../../assets/images/profile.svg';
import Logo2 from '../../../assets/images/logo2.svg';
import Toggle1 from '../../../assets/images/icon_togle1.svg';
import Toggle2 from '../../../assets/images/icon_togle2.svg';

const IS_MOCK = true;

const MOCK_DETAIL = {
    deposit: { amount: 100000, ascentDefence: 10000 },
    weeks: [
        {
            week: 1,
            days: [
                {
                    day: 'TUESDAY',
                    sessionDate: '2026-06-24',
                    sessionTitles: 'HTML/CSS 기초, Git 기초',
                    attendances: [
                        { attendanceId: 1, attendanceOrder: '1차', attended: true },
                        { attendanceId: 2, attendanceOrder: '2차', attended: true },
                        { attendanceId: 3, attendanceOrder: '3차', attended: true },
                    ],
                    assignments: [
                        { assignmentItemId: 1, title: '코딩앵무 클론 코딩', submitted: 'SUBMITTED' },
                        { assignmentItemId: 2, title: '피로그래밍 페이지 클론 코딩', submitted: 'SUBMITTED' },
                    ],
                },
                {
                    day: 'THURSDAY',
                    sessionDate: '2026-06-26',
                    sessionTitles: 'JS 기초, JS 심화',
                    attendances: [
                        { attendanceId: 4, attendanceOrder: '1차', attended: false },
                        { attendanceId: 5, attendanceOrder: '2차', attended: false },
                        { attendanceId: 6, attendanceOrder: '3차', attended: false },
                    ],
                    assignments: [
                        { assignmentItemId: 3, title: '코딩앵무 클론 코딩', submitted: 'NOT_SUBMITTED' },
                    ],
                },
                {
                    day: 'SATURDAY',
                    sessionDate: '2026-06-28',
                    sessionTitles: 'DB 개론',
                    attendances: [
                        { attendanceId: 7, attendanceOrder: '1차', attended: false },
                        { attendanceId: 8, attendanceOrder: '2차', attended: false },
                        { attendanceId: 9, attendanceOrder: '3차', attended: false },
                    ],
                    assignments: [],
                },
            ],
        },
        { week: 2, days: [] },
        { week: 3, days: [] },
        { week: 4, days: [] },
        { week: 5, days: [] },
    ],
};

const dayLabel = { TUESDAY: 'TUE', THURSDAY: 'THU', SATURDAY: 'SAT' };
const statusOptions = ['SUBMITTED', 'LATE', 'NOT_SUBMITTED'];
const statusLabel = { SUBMITTED: '성공', LATE: '미달', NOT_SUBMITTED: '실패' };

// 커리큘럼 데이터에서 날짜별 세션 제목 추출
function extractSessionTitles(curriculums, sessionDate) {
    const day = curriculums.find(c => c.sessionDate === sessionDate);
    if (!day || !day.sessions) return '';
    return day.sessions.map(s => s.title).join(', ');
}

function WeekBlock({ weekData, onChange }) {
    const [isOpen, setIsOpen] = useState(false);
    const [openDays, setOpenDays] = useState({});

    const toggleDay = (day) => {
        setOpenDays(prev => ({ ...prev, [day]: !prev[day] }));
    };

    return (
        <div className={styles.weekBlock}>
            <div className={styles.weekHeader} onClick={() => setIsOpen(p => !p)}>
                <div className={styles.weekLeft}>
                    <img src={Logo2} className={styles.weekLogo} alt="logo" />
                    <span className={styles.weekLabel}>WEEK {weekData.week}</span>
                </div>
                <img
                    src={Toggle1}
                    className={`${styles.toggleIcon} ${isOpen ? styles.toggleOpen : ''}`}
                    alt="toggle"
                />
            </div>

            {isOpen && (
                <div className={styles.weekBody}>
                    {weekData.days.length === 0 && (
                        <div className={styles.empty}>데이터가 없습니다.</div>
                    )}
                    {weekData.days.map((day, i) => (
                        <div key={i} className={styles.dayBlock}>
                            <div className={styles.dayHeader} onClick={() => toggleDay(day.day)}>
                                <div className={styles.dayLeft}>
                                    <span className={styles.dayLabel}>{dayLabel[day.day]}</span>
                                    <span className={styles.sessionDate}>{day.sessionTitles || day.sessionDate}</span>
                                </div>
                                <img
                                    src={Toggle2}
                                    className={`${styles.toggleIcon2} ${openDays[day.day] ? styles.toggleOpen : ''}`}
                                    alt="toggle"
                                />
                            </div>

                            {openDays[day.day] && (
                                <div className={styles.dayBody}>
                                    {/* 출석 */}
                                    <div className={styles.statusGroup}>
                                        <span className={styles.sectionLabel}>출석</span>
                                        <div className={styles.statusItems}>
                                            {day.attendances.map((att, j) => (
                                                <div key={j} className={styles.statusItem}>
                                                    <span className={styles.itemLabel}>{att.attendanceOrder}</span>
                                                    <select
                                                        className={styles.select}
                                                        value={att.attended ? 'true' : 'false'}
                                                        onChange={e => onChange('attendance', weekData.week, day.day, att.attendanceId, e.target.value === 'true')}
                                                    >
                                                        <option value="true">성공</option>
                                                        <option value="false">실패</option>
                                                    </select>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {/* 과제 */}
                                    {day.assignments.length > 0 && (
                                        <div className={styles.statusGroup}>
                                            <span className={styles.sectionLabel}>과제</span>
                                            <div className={styles.statusItems}>
                                                {day.assignments.map((asg, j) => (
                                                    <div key={j} className={styles.statusItem}>
                                                        <span className={styles.itemLabel}>{asg.title}</span>
                                                        <select
                                                            className={styles.select}
                                                            value={asg.submitted}
                                                            onChange={e => onChange('assignment', weekData.week, day.day, asg.assignmentItemId, e.target.value)}
                                                        >
                                                            {statusOptions.map(s => (
                                                                <option key={s} value={s}>{statusLabel[s]}</option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}

                                    <button className={styles.saveWeekBtn}>저장하기</button>
                                </div>
                            )}

                            {i < weekData.days.length - 1 && <hr className={styles.divider} />}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

function StudentDetail() {
    const { userId } = useParams();
    const location = useLocation();
    const studentName = location.state?.name || '수강생';

    const [data, setData] = useState(null);
    const [defence, setDefence] = useState('');

    useEffect(() => {
        if (IS_MOCK) {
            setData(MOCK_DETAIL);
            setDefence(MOCK_DETAIL.deposit.ascentDefence.toString());
            return;
        }

        const fetchData = async () => {
            // TODO: GET /api/admin/{userId}/deposit/view
            // TODO: GET /api/admin/admin/student/{userId}/status/{week} (1~5주차)

            // 커리큘럼에서 세션 제목 가져오기
            const curriculumRes = await fetch('/api/curriculums');
            const curriculums = await curriculumRes.json();

            // weeks 데이터에 sessionTitles 추가
            // const mergedWeeks = weeks.map(w => ({
            //     ...w,
            //     days: w.days.map(d => ({
            //         ...d,
            //         sessionTitles: extractSessionTitles(curriculums, d.sessionDate),
            //     }))
            // }));
        };
        fetchData();
    }, [userId]);

    const handleSaveDefence = async () => {
        if (IS_MOCK) { alert('저장됨 (임시)'); return; }
        // TODO: PUT /api/admin/{userId}/deposit-defend
    };

    const handleStatusChange = (type, week, day, id, value) => {
        setData(prev => {
            const newWeeks = prev.weeks.map(w => {
                if (w.week !== week) return w;
                return {
                    ...w,
                    days: w.days.map(d => {
                        if (d.day !== day) return d;
                        if (type === 'attendance') {
                            return {
                                ...d,
                                attendances: d.attendances.map(a =>
                                    a.attendanceId === id ? { ...a, attended: value } : a
                                ),
                            };
                        } else {
                            return {
                                ...d,
                                assignments: d.assignments.map(a =>
                                    a.assignmentItemId === id ? { ...a, submitted: value } : a
                                ),
                            };
                        }
                    }),
                };
            });
            return { ...prev, weeks: newWeeks };
        });
    };

    const handleSaveAll = async () => {
        if (IS_MOCK) { alert('전체 저장됨 (임시)'); return; }
        // TODO: PATCH /api/admin/users/{userId}/weeks/{week} 주차별로 호출
    };

    if (!data) return null;

    return (
        <div className={styles.container}>
            {IS_MOCK && (
                <div className={styles.mockBanner}>
                    ⚠️ 현재 임시 데이터로 표시 중입니다.
                </div>
            )}

            <div className={styles.card}>
                <div className={styles.profileArea}>
                    <img src={ProfileImg} className={styles.profileImg} alt="profile" />
                    <div className={styles.profileName}>{studentName}</div>
                </div>

                <div className={styles.depositRow}>
                    <div className={styles.depositBoxGreen}>
                        <div className={styles.depositLabel}>잔여 보증금</div>
                        <div className={styles.depositValue}>{data.deposit.amount.toLocaleString()}원</div>
                    </div>
                    <div className={styles.depositBoxGray}>
                        <div className={styles.depositLabel}>보증금 방어권</div>
                        <div className={styles.depositEditRow}>
                            <input
                                className={styles.defenceInput}
                                value={defence}
                                onChange={e => setDefence(e.target.value)}
                            />
                            <span className={styles.won}>원</span>
                            <button className={styles.saveBtn} onClick={handleSaveDefence}>SAVE</button>
                        </div>
                    </div>
                </div>

                {data.weeks.map((w, i) => (
                    <WeekBlock key={i} weekData={w} onChange={handleStatusChange} />
                ))}

                <button className={styles.saveAllBtn} onClick={handleSaveAll}>전체 저장하기</button>
            </div>
        </div>
    );
}

export default StudentDetail;