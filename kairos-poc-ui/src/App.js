// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import SportsAtlasPage from './pages/SportsAtlasPage';
import CulturalArchivePage from './pages/CulturalArchivePage';
import FacilitiesPage from './pages/FacilitiesPage';
import './App.css';

// We'll create these page components in the next step.
// For now, we'll just use placeholders.


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          {/* Index route redirects to the archive page by default */}
          <Route index element={<Navigate to="/archive" replace />} />
          <Route path="archive" element={<CulturalArchivePage />} />
          <Route path="facilities" element={<FacilitiesPage />} />
          <Route path="atlas" element={<SportsAtlasPage />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;