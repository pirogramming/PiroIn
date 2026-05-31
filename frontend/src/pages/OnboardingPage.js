import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './OnboardingPage.module.css';
import logo from '../assets/images/logo.png';

function OnboardingPage() {
  const navigate = useNavigate();

  useEffect(() => {
      document.title = "PIROIN";

      const timer = setTimeout(() => {
          const token = localStorage.getItem('token');
          if (token) {
              navigate('/pirocheck');
          } else {
              navigate('/login');
          }
      }, 2000);
      return () => clearTimeout(timer);
  }, []);

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>PIROIN</h1>
      <div className={styles.logoWrap}>
        <img src={logo} alt="로고" />
      </div>
      <p className={styles.sub}>"피로그래밍의 모든 것, 피로인에서"</p>
      <p className={styles.sub}>피로인들을 위한, 세션 통합 관리 플랫폼</p>
    </div>
  );
}

export default OnboardingPage;