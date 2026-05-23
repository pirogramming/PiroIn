import Header from './Header';
import { Outlet } from 'react-router-dom';

function Layout({ headerType }) {
    return (
        <div style={{ background: headerType === 'dark' ? '#111111' : '#ffffff', minHeight: '100vh' }}>
            <Header type={headerType} />
            <Outlet />
        </div>
    );
}

export default Layout;