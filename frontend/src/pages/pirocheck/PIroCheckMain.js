import { useNavigate } from 'react-router-dom';
import styles from './PIroCheckMain.module.css';
import { useState, useEffect } from 'react';

function PIroCheckMain() {
    const navigate = useNavigate();
    const role = localStorage.getItem('role') || 'MEMBER';

    const adminMenus = [
        { label: '출석 관리', path: '/pirocheck/attendance' },
        { label: '과제 관리', path: '/pirocheck/assignment' },
        { label: '수강생 관리', path: '/pirocheck/students' },
    ];

    const memberMenus = [
        { label: 'ATTENDANCE CHECK', path: '/pirocheck/attendance' },
        { label: 'ASSIGNMENT CHECK', path: '/pirocheck/assignment' },
        { label: 'DEPOSIT CHECK', path: '/pirocheck/deposit' },
    ];

    const menus = role === 'ADMIN' ? adminMenus : memberMenus;

    useEffect(() => {
        document.title = "피로체크 | PIROIN";
      }, []);

    return (
        <div className={styles.container}>
            {menus.map((menu, i) => (
                <button
                    key={i}
                    className={styles.menuBtn}
                    onClick={() => navigate(menu.path)}
                >
                    {menu.label}
                </button>
            ))}
        </div>
    );
}

export default PIroCheckMain;