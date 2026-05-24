import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/login/LoginPage';
import OnboardingPage from './pages/OnboardingPage';
import QnAMainPage from './pages/qna/QnAMainPage';
import QnAListPage from './pages/qna/QnAListPage';
import QnADetailPage from './pages/qna/QnADetailPage';
import CurriculumPage from './pages/curriculum/CurriculumPage';
import CurriculumCreate from './pages/curriculum/CurriculumCreate';

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
          <Route path="/curriculums" element={<CurriculumPage />} />
          {/* 임시 API (role 연결 전이라 create 페이지 일단 따로 만듦) */}
          <Route path="/curriculums/create" element={<CurriculumCreate />} />
        </Route>

        {/* 다크 헤더 페이지 */}
        <Route element={<Layout headerType="dark" />}>
            <Route path="/pirocheck" element={<PiroCheckMain />}/>
            <Route path="/pirocheck/attendance" element={<Attendance />}/>
            <Route path="/pirocheck/assignment" element={<Assignment />}/>
            <Route path="/pirocheck/deposit" element={<Deposit />}/>
            <Route path="/pirocheck/students" element={<StudentList />}/>
            <Route path="/pirocheck/students/:userId" element={<StudentDetail />}/>
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;