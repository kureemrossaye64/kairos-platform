// src/components/CsvIngestForm.js
import React, { useState } from 'react';
import { Box, Button, Typography, CircularProgress, Alert } from '@mui/material';
import { uploadCsv } from '../services/api';

export default function CsvIngestForm() {
    const [file, setFile] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [feedback, setFeedback] = useState({ type: '', message: '' });

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            setFeedback({ type: 'error', message: 'Please select a CSV file.' });
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        setIsLoading(true);
        setFeedback({ type: '', message: '' });

        try {
            await uploadCsv(formData);
            setFeedback({ type: 'success', message: 'CSV file accepted for processing! The database will be updated shortly.' });
            setFile(null);
            document.getElementById('csv-file-input').value = null;
        } catch (error) {
            console.error("Error uploading file:", error);
            setFeedback({ type: 'error', message: error.response?.data?.message || 'Upload failed. Please try again.' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 500 }}
        >
            <Typography variant="h6">Upload Performance Data</Typography>
            <Button
                variant="contained"
                component="label"
            >
                {file ? `Selected: ${file.name}` : 'Select CSV File'}
                <input
                    id="csv-file-input"
                    type="file"
                    hidden
                    accept=".csv, text/csv"
                    onChange={handleFileChange}
                />
            </Button>
            <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={isLoading || !file}
                sx={{ mt: 2 }}
            >
                {isLoading ? <CircularProgress size={24} /> : 'Upload & Process'}
            </Button>
            {feedback.message && (
                <Alert severity={feedback.type} sx={{ mt: 2 }}>{feedback.message}</Alert>
            )}
        </Box>
    );
}