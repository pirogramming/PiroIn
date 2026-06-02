import { useState, useEffect, useCallback } from 'react';
import { NavLink } from 'react-router-dom';
import styles from './Header.module.css';

function Header({ type }) {
    const [menuOpen, setMenuOpen] = useState(false);

    const closeMenu = useCallback(() => setMenuOpen(false), []);

    useEffect(() => {
        const mq = window.matchMedia('(min-width: 1025px)');
        const handler = (e) => { if (e.matches) closeMenu(); };
        mq.addEventListener('change', handler);
        return () => mq.removeEventListener('change', handler);
    }, [closeMenu]);

    useEffect(() => {
        document.body.style.overflow = menuOpen ? 'hidden' : '';
        return () => { document.body.style.overflow = ''; };
    }, [menuOpen]);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        window.location.href = '/login';
    };

    const themeClass = type === 'dark' ? styles.dark : styles.light;

    return (
        <>
            <header className={`${styles.header} ${themeClass}`}>
                <NavLink to="/" className={styles.logo}>PIROIN</NavLink>

                <nav className={styles.nav}>
                    <NavLink to="/pirocheck"  className={({ isActive }) => isActive ? styles.active : ''}>PIROCHECK</NavLink>
                    <NavLink to="/sessions"   className={({ isActive }) => isActive ? styles.active : ''}>Q&A</NavLink>
                    <NavLink to="/curriculum" className={({ isActive }) => isActive ? styles.active : ''}>커리큘럼</NavLink>
                </nav>

                <button className={styles.logoutBtn} onClick={handleLogout}>
                    로그아웃
                </button>

                <button
                    className={`${styles.hamburger} ${menuOpen ? styles.hamburgerOpen : ''}`}
                    onClick={() => setMenuOpen((prev) => !prev)}
                    aria-label={menuOpen ? '메뉴 닫기' : '메뉴 열기'}
                    aria-expanded={menuOpen}
                >
                    <span />
                    <span />
                    <span />
                </button>
            </header>

            {/* 오버레이 */}
            <div
                className={`${styles.overlay} ${menuOpen ? styles.overlayVisible : ''}`}
                onClick={closeMenu}
                aria-hidden="true"
            />

            {/* 드로어: themeClass 추가로 CSS 변수 상속 */}
            <nav
                className={`${styles.drawer} ${themeClass} ${menuOpen ? styles.drawerOpen : ''}`}
                aria-hidden={!menuOpen}
            >
                <button className={styles.drawerCloseBtn} onClick={closeMenu} aria-label="메뉴 닫기">✕</button>
                <NavLink to="/pirocheck"  className={({ isActive }) => isActive ? styles.active : ''} onClick={closeMenu}>PIROCHECK</NavLink>
                <NavLink to="/sessions"   className={({ isActive }) => isActive ? styles.active : ''} onClick={closeMenu}>Q&A</NavLink>
                <NavLink to="/curriculum" className={({ isActive }) => isActive ? styles.active : ''} onClick={closeMenu}>커리큘럼</NavLink>

                <button className={styles.drawerLogoutBtn} onClick={handleLogout}>
                    로그아웃
                </button>
            </nav>
        </>
    );
}

export default Header;