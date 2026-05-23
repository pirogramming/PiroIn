import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/login/LoginPage';
import OnboardingPage from './pages/OnboardingPage';
import QnAMainPage from './pages/qna/QnAMainPage';
import QnAListPage from './pages/qna/QnAListPage';
import QnADetailPage from './pages/qna/QnADetailPage';
import CurriculumPage from './pages/curriculum/CurriculumPage';
import PiroCheckMain from './pages/pirocheck/PIroCheckMain';
import Attendance from './pages/pirocheck/attendance/Attendance'
import Assignment from './pages/pirocheck/assignment/Assignment';
import Deposit from './pages/pirocheck/deposit/Deposit';

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* 헤더 없는 페이지 */}
        <Route path="/" element={<OnboardingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />

        {/* 라이트 헤더 페이지 */}
        <Route element={<Layout headerType="light" />}>
          <Route path="/sessions" element={<QnAMainPage />} />
          <Route path="/sessions/questions" element={<QnAListPage />} />
          <Route path="/sessions/questions/:id" element={<QnADetailPage />} />
          <Route path="/curriculum" element={<CurriculumPage />} />
        </Route>

        {/* 다크 헤더 페이지 */}
        <Route element={<Layout headerType="dark" />}>
            <Route path="/pirocheck" element={<PiroCheckMain />}/>
            <Route path="/pirocheck/attendance" element={<Attendance />}/>
            <Route path="/pirocheck/assignment" element={<Assignment />}/>
            <Route path="/pirocheck/deposit" element={<Deposit />}/>
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;