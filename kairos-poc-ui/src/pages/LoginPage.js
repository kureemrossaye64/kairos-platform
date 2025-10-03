import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { loginUser } from '../services/api'; // We'll create this function
import { useNavigate, Link } from 'react-router-dom';
import { Container, Box, TextField, Button, Typography, CircularProgress, Alert } from '@mui/material';

export default function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const userData = await loginUser({ username, password });
            login(userData);
            navigate('/chat'); // Redirect to the main chat page on successful login
        } catch (err) {
            setError(err.response?.data || 'Login failed. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <Box sx={{ marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Typography component="h1" variant="h5">Sign In to KAIROS Nexus</Typography>
                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
                    <TextField margin="normal" required fullWidth id="username" label="Email Address" name="username" autoComplete="email" autoFocus value={username} onChange={e => setUsername(e.target.value)} />
                    <TextField margin="normal" required fullWidth name="password" label="Password" type="password" id="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} />
                    {error && <Alert severity="error" sx={{ width: '100%', mt: 2 }}>{error}</Alert>}
                    <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={loading}>
                        {loading ? <CircularProgress size={24} /> : 'Sign In'}
                    </Button>
                    <Typography variant="body2" align="center">
                        Don't have an account? {' '}
                        <Link to="/register" style={{ textDecoration: 'none' }}>
                             Sign Up
                        </Link>
                    </Typography>
                </Box>
            </Box>
        </Container>
    );
}