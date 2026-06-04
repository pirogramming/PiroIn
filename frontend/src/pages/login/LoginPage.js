import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';

function LoginPage() {
  const navigate = useNavigate();
  const [focused, setFocused] = useState('');
  const [form, setForm] = useState({ name: '', password: '' });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleLogin = async () => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('role', data.role);
        localStorage.setItem('name', data.name);
        navigate('/sessions');
      } else {
        alert('이름 또는 비밀번호가 올바르지 않습니다.');
      }
    } catch (error) {
      alert('서버 오류가 발생했습니다.');
    }
  };

  // 엔터키 로그인
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleLogin();
  };

  useEffect(() => {
    document.title = "로그인 | PIROIN";
  }, []);

  return (
    <div className={styles.container}>
      <h1 className={styles.title} onClick={() => navigate('/')}>PIROIN</h1>
      <div className={styles.form}>
        <input
          type="text"
          name="name"
          placeholder="이름"
          value={form.name}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          className={`${styles.input} ${focused === 'name' ? styles.inputFocused : ''}`}
          onFocus={() => setFocused('name')}
          onBlur={() => setFocused('')}
        />
        <input
          type="password"
          name="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          className={`${styles.input} ${focused === 'pw' ? styles.inputFocused : ''}`}
          onFocus={() => setFocused('pw')}
          onBlur={() => setFocused('')}
        />
        <button className={styles.button} onClick={handleLogin}>로그인</button>
      </div>
    </div>
  );
}

export default LoginPage;