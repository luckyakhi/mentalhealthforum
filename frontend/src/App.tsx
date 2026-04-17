import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ThreadDetailPage from './pages/ThreadDetailPage';
import CreateThreadPage from './pages/CreateThreadPage';
import UserProfilePage from './pages/UserProfilePage';
import SearchPage from './pages/SearchPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/thread/create" element={
              <ProtectedRoute>
                <CreateThreadPage />
              </ProtectedRoute>
            } />
            <Route path="/thread/:id" element={<ThreadDetailPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/profile/:userId" element={<UserProfilePage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
