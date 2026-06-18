const LOGIN_PATH = '/login';

// 401 응답을 받았을 때 한 번만 처리되도록 막아주는 플래그
// (같은 화면에서 여러 API가 동시에 401을 받아도 리다이렉트는 한 번만 실행됨)
let isRedirectingToLogin = false;

function redirectToLogin() {
    if (isRedirectingToLogin) return;
    isRedirectingToLogin = true;

    localStorage.removeItem('token');
    localStorage.removeItem('role');

    // 이미 로그인 페이지에 있다면 또 이동시키지 않음
    if (window.location.pathname !== LOGIN_PATH) {
        window.location.href = LOGIN_PATH;
    }
}

export async function authFetch(url, options = {}) {
    const token = localStorage.getItem('token');

    const res = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers,
        },
    });

    if (res.status === 401) {
        redirectToLogin();
        // 호출부에서 추가로 catch 처리할 수 있도록 에러를 던짐
        throw new Error('인증이 만료되었어요. 다시 로그인해주세요.');
    }

    return res;
}