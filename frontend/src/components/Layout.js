import Header from './Header';
import { Outlet } from 'react-router-dom';

function Layout({ headerType }) {
    return (
        <>
            <Header type={headerType} />
            <Outlet />
        </>
    );
}

export default Layout;