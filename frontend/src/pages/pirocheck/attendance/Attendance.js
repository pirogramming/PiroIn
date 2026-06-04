import { useState, useEffect } from 'react';
import { authFetch } from '../../../utils/Api';
import styles from './Attendance.module.css';
import CloverGreen from '../../../assets/images/CloverGreen.svg';
import CloverRed from '../../../assets/images/CloverRed.svg';
import CloverEmpty from '../../../assets/images/CloverEmpty.svg'; 
import Coin1 from '../../../assets/images/Coin1.svg';
import Coin2 from '../../../assets/images/Coin2.svg';
import Coin3 from '../../../assets/images/Coin3.svg';
import AngryIcon from '../../../assets/images/AngryIcon.svg';

function cloverForSlot(status) {
    if (status === true)  return <img src={CloverGreen} className={styles.cloverSvg} alt="출석" />;
    if (status === false) return <img src={CloverRed} className={styles.cloverSvg} alt="결석" />;
    return <img src={CloverEmpty} className={styles.cloverSvg} alt="미정" />;
}

function historyIcon(slots) {
    const successCount = slots.filter(s => s.status === true).length;
    if (successCount === 3) return <img src={Coin3} className={styles.histSvg} alt="3회 출석" />;
    if (successCount === 2) return <img src={Coin2} className={styles.histSvg} alt="2회 출석" />;
    if (successCount === 1) return <img src={Coin1} className={styles.histSvg} alt="1회 출석" />;
    return <img src={AngryIcon} className={styles.histSvg} alt="결석" />;
}

// ── ADMIN 뷰 ──────────────────────────────────────────
function AdminView() {
    const [code, setCode] = useState(null);
    const [hasCode, setHasCode] = useState(false);
    const [message, setMessage] = useState('');

    useEffect(() => {
        const fetchActiveCode = async () => {
            try {
                const res = await authFetch('/api/admin/attendance/active-code');
                if (res.ok) {
                    const data = await res.json();
                    if (!data.isExpired) {
                        setCode(data.code);
                        setHasCode(true);
                    }
                }
            } catch (e) {}
        };
        fetchActiveCode();
    }, []);

    const handleGenerate = async () => {
        const res = await authFetch('/api/admin/attendance/start', { method: 'POST' });
        const data = await res.json();
        setCode(data.code);
        if (data.isSuccess) {
            setCode(data.result.code);
            setHasCode(true);
            setMessage('');
        } else {
            setMessage(data.message);
        }
    };

    const handleExpire = async () => {
        await authFetch('/api/admin/attendance/active-code/expire', { method: 'PUT' });
        setCode(null);
        setHasCode(false);
    };

    return (
        <>
            <div className={styles.title}>ATTENDANCE CHECK</div>
            <div className={styles.codebox}>
                {[0, 1, 2, 3].map((i) => (
                    <div key={i} className={styles.code}>
                        {code ? code[i] : ''}
                    </div>
                ))}
            </div>

            {message && (
                <div className={styles.adminMsg}>
                    {message}
                </div>
            )}

            <div className={styles.manage}>
                <button className={styles.createBtn} onClick={handleGenerate}>
                    {hasCode ? '재생성' : '출석코드 생성'}
                </button>
                {hasCode && (
                    <button className={styles.createBtn} onClick={handleExpire}>
                        종료
                    </button>
                )}
                <a className={styles.manageLink} href="/pirocheck/students">출석 관리</a>
            </div>
        </>
    );
}

// ── MEMBER 뷰 ─────────────────────────────────────────
function MemberView() {
    const [inputCode, setInputCode] = useState('');
    const [message, setMessage] = useState('');
    const [todaySlots, setTodaySlots] = useState([]);
    const [history, setHistory] = useState([]);

    useEffect(() => {
    const today = new Date().toISOString().split('T')[0];

    authFetch(`/api/attendance/user/date?date=${today}`)
        .then(r => r.json())
        .then(d => setTodaySlots(d.data || []))
        .catch(() => setTodaySlots([]));

    const dayOrder = ['TUESDAY', 'THURSDAY', 'SATURDAY'];

    const defaultHistory = [1, 2, 3, 4, 5].map(week => ({
        week,
        days: dayOrder.map(day => ({
            day,
            slots: []
        }))
    }));

    authFetch('/api/attendance/user')
        .then(r => r.json())
        .then(data => {
            const apiData = data.data || [];

            const merged = defaultHistory.map(def => {
                const foundWeek = apiData.find(
                    item => Number(item.week) === Number(def.week)
                );

                if (!foundWeek) return def;

                return {
                    week: def.week,
                    days: dayOrder.map(dayName => {
                        const foundDay = foundWeek.days?.find(
                            day => day.day === dayName
                        );

                        return {
                            day: dayName,
                            slots: foundDay?.slots || []
                        };
                    })
                };
            });

            setHistory(merged);
        })
        .catch(() => setHistory(defaultHistory));
}, []);

    const handleSubmit = async () => {
        if (!inputCode.trim()) return;
        const res = await authFetch('/api/attendance/mark', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: inputCode }),
        });
        const data = await res.json();
        const result = data.data;

        if (result.statusCode === 'SUCCESS') {
            setMessage('출석 성공!');
            const today = new Date().toISOString().split('T')[0];
            authFetch(`/api/attendance/user/date?date=${today}`)
                .then(r => r.json())
                .then(d => setTodaySlots(d.data || []));
        } else if (result.statusCode === 'INVALID_CODE') {
            setMessage('출석 코드를 확인해주세요.');
        } else {
            setMessage(result.message); 
        }

        setInputCode('');
    };

    const displaySlots = [0, 1, 2].map(i => todaySlots[i] ?? { status: null });

    return (
        <>
            <div className={styles.title}>ATTENDANCE CHECK</div>

            <div className={styles.inputRow}>
                <input
                    className={styles.codeInput}
                    placeholder="출석 코드를 입력하세요."
                    value={inputCode}
                    onChange={(e) => setInputCode(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                    maxLength={4}
                />
                <button className={styles.submitBtn} onClick={handleSubmit}>출석</button>
            </div>

            {message && <div className={styles.msg}>{message}</div>}

            <div className={styles.cloverRow}>
                {displaySlots.map((slot, i) => (
                    <div key={i}>{cloverForSlot(slot.status)}</div>
                ))}
            </div>

            <div className={styles.historyBox}>
                {history.map((row, i) => (
                    <div key={i} className={styles.historyRow}>
                        <span className={styles.weekLabel}>{row.week}주차</span>
                        <div className={styles.historySlots}>
                            {row.days.map((day) => (
                                <div key={day.day}>
                                    {historyIcon(day.slots)}
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
}

// ── 메인 컴포넌트 ─────────────────────────────────────
function Attendance() {
    const role = localStorage.getItem('role') || 'MEMBER';

    return (
        <div className={styles.container}>
            {role === "ADMIN" ? <AdminView /> : <MemberView />}
        </div>
    );
}

export default Attendance;
