import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './StudentList.module.css';
import ArrowRight from '../../../assets/images/icon_arrow_right.svg';

const IS_MOCK = false;

function StudentList() {
    const navigate = useNavigate();
    const [students, setStudents] = useState([]);
    const [search, setSearch] = useState('');

    const fetchStudents = async (keyword = '') => {
        try {
            const url = keyword
                ? `/api/admin/studentlist/search?name=${keyword}`
                : '/api/admin/studentlist';
            const res = await fetch(url);
            const data = await res.json();
            setStudents(Array.isArray(data) ? data : data.data || []);
        } catch (e) {} 
    };

    useEffect(() => { fetchStudents(); }, []);

    const handleSearch = () => fetchStudents(search);

    return (
        <div className={styles.container}>

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
                        <img src={ArrowRight} className={styles.arrow} alt="arrow" />
                    </button>
                ))}
            </div>
        </div>
    );
}

export default StudentList;