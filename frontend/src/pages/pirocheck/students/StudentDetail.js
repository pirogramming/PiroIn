import { useState, useEffect } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { authFetch } from '../../../utils/Api';
import styles from './StudentDetail.module.css';
import ProfileImg from '../../../assets/images/profile.svg';
import Logo2 from '../../../assets/images/logo2.svg';
import Toggle1 from '../../../assets/images/icon_togle1.svg';
import Toggle2 from '../../../assets/images/icon_togle2.svg';

const IS_MOCK = false;

const dayLabel = { TUESDAY: 'TUE', THURSDAY: 'THU', SATURDAY: 'SAT' };
const statusOptions = ['SUBMITTED', 'LATE', 'NOT_SUBMITTED'];
const statusLabel = { SUBMITTED: '성공', LATE: '미달', NOT_SUBMITTED: '실패' };

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
                    {(!weekData.days || weekData.days.length === 0) && (
                        <div className={styles.empty}>데이터가 없습니다.</div>
                    )}
                    {(weekData.days || []).map((day, i) => (
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

        const fetchData = async () => {
            try {
                // 보증금 조회
                const depositRes = await authFetch(`/api/deposit/${userId}/deposit/view`);
                const depositData = await depositRes.json();
                setDefence(depositData.ascentDefence.toString());

                // 주차별 출석/과제 조회
                const weekResults = await Promise.all(
                    [1, 2, 3, 4, 5].map(w =>
                        authFetch(`/api/admin/admin/student/${userId}/status/${w}`)
                            .then(r => r.json())
                            .catch(() => ({ week: w, days: [] }))
                    )
                );

                setData({
                    deposit: depositData,
                    weeks: weekResults,
                });
            } catch (e) {}
        };
        fetchData();
    }, [userId]);

    const handleSaveDefence = async () => {
        await authFetch(`/api/deposit/${userId}/deposit/defence`, {
            method: 'PATCH',
            body: JSON.stringify({ ascentDefence: Number(defence) }),
        });
        alert('저장됐습니다!');
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
        await Promise.all(
            data.weeks.map(w =>
                Promise.all(
                    w.days.map(d => {
                        const body = {
                            attendances: d.attendances.map(a => ({
                                attendanceId: a.attendanceId,
                                status: a.attended,
                            })),
                            assignments: d.assignments.map(a => ({
                                assignmentItemId: a.assignmentItemId,
                                submitted: a.submitted,
                            })),
                        };
                        return authFetch(`/api/admin/users/${userId}/weeks/${w.week}`, {
                            method: 'PATCH',
                            body: JSON.stringify(body),
                        });
                    })
                )
            )
        );
        alert('저장됐습니다!');
    };

    if (!data) return null;

    return (
        <div className={styles.container}>
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