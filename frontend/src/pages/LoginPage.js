import { useState } from 'react';
import styles from './LoginPage.module.css';

function LoginPage() {
  const [focused, setFocused] = useState('');

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>PIROIN</h1>
      <div className={styles.form}>
        <input
          type="text"
          placeholder="이름"
          className={`${styles.input} ${focused === 'name' ? styles.inputFocused : ''}`}
          onFocus={() => setFocused('name')}
          onBlur={() => setFocused('')}
        />
        <input
          type="password"
          placeholder="비밀번호"
          className={`${styles.input} ${focused === 'pw' ? styles.inputFocused : ''}`}
          onFocus={() => setFocused('pw')}
          onBlur={() => setFocused('')}
        />
        <button className={styles.button}>로그인</button>
      </div>
    </div>
  );
}

export default LoginPage;