import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/login/LoginPage';
import OnboardingPage from './pages/OnboardingPage';
import QnAMainPage from './pages/qna/QnAMainPage';
import QnAListPage from './pages/qna/QnAListPage';
import QnADetailePage from './pages/qna/QnADetailePage';
import CurriculumPage from './pages/curriculum/CurriculumPage';
import PiroCheckMain from './pages/pirocheck/PIroCheckMain';
import Attendance from './pages/pirocheck/attendance/Attendance'

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
          <Route path="/sessions/questions/:id" element={<QnADetailePage />} />
          <Route path="/curriculum" element={<CurriculumPage />} />
        </Route>
        
        {/* 다크 헤더 페이지 */}
        <Route element={<Layout headerType="dark" />}>
          <Route path="/pirocheck" element={<PiroCheckMain />}/>
          <Route path="/pirocheck/attendance" element={<Attendance />}/>
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;