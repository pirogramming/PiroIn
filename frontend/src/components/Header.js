import { NavLink } from 'react-router-dom';
import styles from './Header.module.css';

function Header() {
    return (
        <header className={styles.header}>
            <NavLink to="/" className={styles.logo}>PIROIN</NavLink>
            <nav className={styles.nav}>
                <NavLink to="/pirocheck" className={({ isActive }) => isActive ? styles.active : ''}>PIROCHECK</NavLink>
                <NavLink to="/sessions" className={({ isActive }) => isActive ? styles.active : ''}>Q&A</NavLink>
                <NavLink to="/curriculum" className={({ isActive }) => isActive ? styles.active : ''}>커리큘럼</NavLink>
            </nav>
        </header>
    );
}

export default Header;