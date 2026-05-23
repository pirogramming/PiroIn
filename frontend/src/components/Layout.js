import Header from './Header';
import { Outlet } from 'react-router-dom';

function Layout({ headerType }) {
    return (
        <div style={{ background: headerType === 'dark' ? '#111111' : 'var(--gray20)', minHeight: '100vh' }}>
            <Header type={headerType} />
            <Outlet />
        </div>
    );
}

export default Layout;