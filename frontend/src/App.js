import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/login/LoginPage';
import OnboardingPage from './pages/OnboardingPage';
import QnAMainPage from './pages/qna/QnAMainPage';
import QnAListPage from './pages/qna/QnAListPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* 헤더 없는 페이지 */}
        <Route path="/" element={<OnboardingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />

        {/* 헤더 있는 페이지 */}
        <Route element={<Layout />}>
          <Route path="/sessions" element={<QnAMainPage />} />
          <Route path="/sessions/questions" element={<QnAListPage />} />
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;