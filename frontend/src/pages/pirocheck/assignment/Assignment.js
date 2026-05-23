import { useState, useEffect } from 'react';
import styles from './Assignment.module.css';
import LogoImg from '../../../assets/images/logo.png';
import EditIcon from '../../../assets/images/icon_edit.svg';
import DeleteIcon from '../../../assets/images/icon_delete.svg';
import StatusO from '../../../assets/images/icon_status_o.svg';
import StatusT from '../../../assets/images/icon_status_triangle.svg';
import StatusX from '../../../assets/images/icon_status_x.svg';

// 요일 변환
const dayMap = {
    TUESDAY: 'TUE',
    THURSDAY: 'THU',
    SATURDAY: 'SAT',
};

// 임시 데이터 (백엔드 연동 전)
const MOCK_DATA = [
    {
        week: '1',
        assignments: [
            { assignmentId: 1, title: '코딩앵무 클론 코딩', week: '1', day: 'TUESDAY', sessionDate: 'HTML/CSS, Git 기초', submitted: 'SUBMITTED' },
            { assignmentId: 2, title: '피로그래밍 페이지 클론 코딩', week: '1', day: 'TUESDAY', sessionDate: 'HTML/CSS, Git 기초', submitted: 'NOT_SUBMITTED' },
            { assignmentId: 3, title: '코딩앵무 클론 코딩', week: '1', day: 'THURSDAY', sessionDate: 'JS', submitted: 'SUBMITTED' },
            { assignmentId: 4, title: '숫자야구 게임', week: '1', day: 'THURSDAY', sessionDate: 'JS', submitted: 'LATE' },
            { assignmentId: 5, title: '파이썬 코딩도장', week: '1', day: 'THURSDAY', sessionDate: 'JS', submitted: 'NOT_SUBMITTED' },
            { assignmentId: 6, title: '아르사 팀플', week: '1', day: 'SATURDAY', sessionDate: 'DB 개론', submitted: 'SUBMITTED' },
        ],
    },
    { week: '2', assignments: [] },
    { week: '3', assignments: [] },
    { week: '4', assignments: [] },
    { week: '5', assignments: [] },
];

const IS_MOCK = true; // 백엔드 연동 시 false로 변경

// 제출 상태 아이콘 (부원용)
function StatusIcon({ status }) {
    if (status === 'SUBMITTED') return <img src={StatusO} className={styles.statusIcon} alt="제출" />;
    if (status === 'LATE') return <img src={StatusT} className={styles.statusIcon} alt="지각제출" />;
    return <img src={StatusX} className={styles.statusIcon} alt="미제출" />;
}

// 세션별 과제 묶기
function groupByDay(assignments) {
    const order = ['TUESDAY', 'THURSDAY', 'SATURDAY'];
    const grouped = {};
    assignments.forEach(a => {
        if (!grouped[a.day]) grouped[a.day] = { day: a.day, sessionDate: a.sessionDate, items: [] };
        grouped[a.day].items.push(a);
    });
    return order.filter(d => grouped[d]).map(d => grouped[d]);
}

// ── 과제 등록/수정 모달 ───────────────────────────────
function AssignmentModal({ item, onClose, onSave }) {
    const isEdit = !!item;
    const [form, setForm] = useState({
        week: item?.week || '1',
        day: item?.day || 'TUESDAY',
        title: item?.title || '',
    });
    const weeks = ['1', '2', '3', '4', '5'];
    const days = ['TUESDAY', 'THURSDAY', 'SATURDAY'];

    const handleSave = async () => {
        const url = isEdit
            ? `/api/assignments/modify/${item.assignmentId}`
            : '/api/assignments/create';
        const method = isEdit ? 'PATCH' : 'POST';
        await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title: form.title, week: form.week, day: form.day }),
        });
        onSave();
        onClose();
    };

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modal}>
                <img src={LogoImg} className={styles.modalLogo} alt="logo" />
                <div className={styles.modalTitle}>ASSIGNMENT</div>
                <div className={styles.modalRow}>
                    <select className={styles.select} value={form.week} onChange={e => setForm({ ...form, week: e.target.value })}>
                        {weeks.map(w => <option key={w} value={w}>{w}주차</option>)}
                    </select>
                    <select className={styles.select} value={form.day} onChange={e => setForm({ ...form, day: e.target.value })}>
                        {days.map(d => <option key={d} value={d}>{dayMap[d]}</option>)}
                    </select>
                    <span className={styles.modalLabel}>과제</span>
                </div>
                <input
                    className={styles.modalInput}
                    placeholder="과제제목"
                    value={form.title}
                    onChange={e => setForm({ ...form, title: e.target.value })}
                />
                <button className={styles.saveBtn} onClick={handleSave}>저장하기</button>
            </div>
        </div>
    );
}

// ── 주차 블록 (공통) ──────────────────────────────────
function WeekBlock({ weekData, role, onEdit, onDelete }) {
    const [isOpen, setIsOpen] = useState(false);
    const grouped = groupByDay(weekData.assignments || []);

    return (
        <div className={styles.weekBlock}>
            <div className={styles.weekHeader} onClick={() => setIsOpen(prev => !prev)}>
                <div className={styles.weekLeft}>
                    <img src={LogoImg} className={styles.logoIcon} alt="logo" />
                    <span className={styles.weekLabel}>WEEK {weekData.week}</span>
                </div>
                <span className={styles.arrow}>{isOpen ? '▲' : '▼'}</span>
            </div>

            {isOpen && (
                <div className={styles.weekBody}>
                    {grouped.length === 0 && (
                        <div className={styles.empty}>등록된 과제가 없습니다.</div>
                    )}
                    {grouped.map((session, j) => (
                        <div key={j} className={styles.session}>
                            <div className={styles.sessionHeader}>
                                <div className={styles.sessionLeft}>
                                    <span className={styles.dayLabel}>{dayMap[session.day]}</span>
                                    {session.sessionDate && <span className={styles.sessionTitle}>{session.sessionDate}</span>}
                                </div>
                                {role === 'ADMIN' && (
                                    <div className={styles.sessionActions}>
                                        <button className={styles.iconBtn} onClick={() => onEdit(session.items[0])}>
                                            <img src={EditIcon} className={styles.actionIcon} alt="수정" />
                                        </button>
                                        <button className={styles.iconBtn} onClick={() => onDelete(session.items[0].assignmentId)}>
                                            <img src={DeleteIcon} className={styles.actionIcon} alt="삭제" />
                                        </button>
                                    </div>
                                )}
                            </div>
                            {session.items.map((item, k) => (
                                <div key={k} className={styles.assignmentRow}>
                                    <span className={styles.assignmentTitle}>{item.title}</span>
                                    {role === 'MEMBER' && <StatusIcon status={item.submitted} />}
                                </div>
                            ))}
                            {j < grouped.length - 1 && <hr className={styles.divider} />}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

// ── 메인 컴포넌트 ─────────────────────────────────────
function Assignment() {
    const role = localStorage.getItem('role') || 'MEMBER';
    const [weeks, setWeeks] = useState([]);
    const [modalItem, setModalItem] = useState(undefined); // undefined=닫힘, null=생성, object=수정

    const fetchAll = async () => {
        if (IS_MOCK) {
            setWeeks(MOCK_DATA);
            return;
        }
        const results = await Promise.all(
            ['1', '2', '3', '4', '5'].map(w =>
                fetch(`/api/assignments/me/${w}`)
                    .then(r => r.json())
                    .catch(() => ({ week: w, assignments: [] }))
            )
        );
        setWeeks(results);
    };

    useEffect(() => { fetchAll(); }, []);

    const handleDelete = async (assignmentId) => {
        if (!window.confirm('삭제하시겠습니까?')) return;
        if (!IS_MOCK) {
            await fetch(`/api/assignments/${assignmentId}`, { method: 'DELETE' });
        }
        fetchAll();
    };

    return (
        <div className={styles.container}>
            {IS_MOCK && (
                <div className={styles.mockBanner}>⚠️ 현재 임시 데이터로 표시 중입니다. 백엔드 연동 후 IS_MOCK을 false로 변경하세요.</div>
            )}
            <div className={styles.title}>ASSIGNMENT CHECK</div>

            {weeks.map((w, i) => (
                <WeekBlock
                    key={i}
                    weekData={w}
                    role={role}
                    onEdit={(item) => setModalItem(item)}
                    onDelete={handleDelete}
                />
            ))}

            {role === 'ADMIN' && (
                <button className={styles.addBtn} onClick={() => setModalItem(null)}>+</button>
            )}

            {modalItem !== undefined && (
                <AssignmentModal
                    item={modalItem}
                    onClose={() => setModalItem(undefined)}
                    onSave={fetchAll}
                />
            )}
        </div>
    );
}

export default Assignment;