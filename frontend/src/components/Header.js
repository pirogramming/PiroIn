import { NavLink } from 'react-router-dom';
import styles from './Header.module.css';

function Header({ type }) {
    return (
        <header className={`${styles.header} ${type === "dark" ? styles.dark : styles.light}`}>
            <NavLink to="/" className={styles.logo}>PIROIN</NavLink>
            <nav className={styles.nav}>
                <NavLink to="/pirocheck" className={({ isActive }) => isActive ? styles.active : ''}>PIROCHECK</NavLink>
                <NavLink to="/sessions" className={({ isActive }) => isActive ? styles.active : ''}>Q&A</NavLink>
                <NavLink to="/curriculum" className={({ isActive }) => isActive ? styles.active : ''}>커리큘럼</NavLink>
            </nav>
            <button className={styles.logoutBtn} onClick={() => {
                localStorage.removeItem('token');
                localStorage.removeItem('role');
                window.location.href = '/login';
            }}>
                로그아웃
            </button>            
        </header>
    );
}

export default Header;