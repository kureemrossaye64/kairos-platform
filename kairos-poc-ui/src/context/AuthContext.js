import React, { createContext, useState, useContext, useEffect } from 'react';
import { nexusApi } from '../services/api'; // We'll update api.js to export this

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // On app load, check for a token in localStorage
        const token = localStorage.getItem('authToken');
        const userData = localStorage.getItem('userData');
        if (token && userData) {
            setUser(JSON.parse(userData));
            // Set the token for all subsequent API requests
            nexusApi.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        }
        setLoading(false);
    }, []);

    const login = (userData) => {
        localStorage.setItem('authToken', userData.token);
        localStorage.setItem('userData', JSON.stringify(userData));
        nexusApi.defaults.headers.common['Authorization'] = `Bearer ${userData.token}`;
        setUser(userData);
    };

    const logout = () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        delete nexusApi.defaults.headers.common['Authorization'];
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

// Custom hook to easily access the auth context
export const useAuth = () => useContext(AuthContext);