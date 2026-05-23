import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './StudentList.module.css';

const MOCK_STUDENTS = [
    { userId: 1, name: '김피로' },
    { userId: 2, name: '이피로' },
    { userId: 3, name: '박피로' },
    { userId: 4, name: '최피로' },
    { userId: 5, name: '정피로' },
];

const IS_MOCK = true;

function StudentList() {
    const navigate = useNavigate();
    const [students, setStudents] = useState([]);
    const [search, setSearch] = useState('');

    const fetchStudents = async (keyword = '') => {
        if (IS_MOCK) {
            if (keyword) {
                setStudents(MOCK_STUDENTS.filter(s => s.name.includes(keyword)));
            } else {
                setStudents(MOCK_STUDENTS);
            }
            return;
        }
        const url = keyword
            ? `/api/admin/studentlist/search?name=${keyword}`
            : '/api/admin/studentlist';
        const res = await fetch(url);
        const data = await res.json();
        setStudents(data);
    };

    useEffect(() => { fetchStudents(); }, []);

    const handleSearch = () => fetchStudents(search);

    return (
        <div className={styles.container}>
            {IS_MOCK && (
                <div className={styles.mockBanner}>
                    ⚠️ 현재 임시 데이터로 표시 중입니다.
                </div>
            )}

            <div className={styles.title}>PIROGRAMMER</div>

            <div className={styles.searchRow}>
                <input
                    className={styles.searchInput}
                    placeholder="수강생을 검색하세요."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleSearch()}
                />
                <button className={styles.searchBtn} onClick={handleSearch}>검색</button>
            </div>

            <div className={styles.list}>
                {students.map((s, i) => (
                    <button
                        key={i}
                        className={styles.studentItem}
                        onClick={() => navigate(`/pirocheck/students/${s.userId}`, { state: { name: s.name } })}
                    >
                        <span className={styles.studentName}>{s.name}</span>
                        <span className={styles.arrow}>›</span>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default StudentList;