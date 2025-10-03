// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import KayaAssistantPage from './pages/KayaAssistantPage';
import IngestionPage from './pages/IngestionPage';
import CrawlerPage from './pages/CrawlerPage';
import LoginPage from './pages/LoginPage';
// import RegisterPage from './pages/RegisterPage';
import './App.css';
import ProtectedRoute from './common/ProtectedRoute';




import ReviewPage from './pages/ReviewPage';


function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />
        {/* <Route path="/register" element={<RegisterPage />} /> */}

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<AppLayout />}>
            <Route index element={<Navigate to="/chat" replace />} />
            <Route path="chat" element={<KayaAssistantPage />} />
            <Route path="ingestion" element={<IngestionPage />} />
            <Route path="crawler" element={<CrawlerPage />} />
            <Route path="review" element={<ReviewPage />} />
          </Route>
        </Route>
      </Routes>
    </Router>
  );
}

export default App;