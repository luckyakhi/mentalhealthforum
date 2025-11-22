// File: frontend/src/App.tsx

import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './pages/LandingPage';
import ForumsPage from './pages/ForumsPage';
import TherapyPage from './pages/TherapyPage';
import HumanTherapyPage from './pages/HumanTherapyPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <Navbar />
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/forums" element={<ForumsPage />} />
          <Route path="/therapy" element={<TherapyPage />} />
          <Route path="/human-therapy" element={<HumanTherapyPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;