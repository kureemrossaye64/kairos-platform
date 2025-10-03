import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate, Outlet } from 'react-router-dom';

export default function ProtectedRoute() {
    const { isAuthenticated } = useAuth();
    
    if (!isAuthenticated) {
        // If not authenticated, redirect to the login page
        return <Navigate to="/login" replace />;
    }
    
    // If authenticated, render the child components (the protected pages)
    return <Outlet />;
}