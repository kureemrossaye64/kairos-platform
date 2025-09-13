// src/components/AudioIngestForm.js
import React, { useState } from 'react';
import { Box, Button, TextField, Select, MenuItem, FormControl, InputLabel, Typography, CircularProgress, Alert } from '@mui/material';
import { uploadAudio } from '../services/api';

const assetTypes = [
    'ORAL_HISTORY',
    'TRADITIONAL_SONG',
    'POEM',
    'FOLKTALE',
    'RECIPE',
    'CRAFT_TECHNIQUE'
];

export default function AudioIngestForm() {
    const [file, setFile] = useState(null);
    const [title, setTitle] = useState('');
    const [assetType, setAssetType] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [feedback, setFeedback] = useState({ type: '', message: '' }); // type can be 'success' or 'error'

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file || !title || !assetType) {
            setFeedback({ type: 'error', message: 'Please fill all fields and select a file.' });
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('title', title);
        formData.append('assetType', assetType);

        setIsLoading(true);
        setFeedback({ type: '', message: '' });

        try {
            await uploadAudio(formData);
            setFeedback({ type: 'success', message: 'File accepted for processing! The agent will have this knowledge shortly.' });
            // Reset form
            setFile(null);
            setTitle('');
            setAssetType('');
            document.getElementById('audio-file-input').value = null;
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
            <Typography variant="h6">Upload a New Audio Asset</Typography>
            <TextField
                label="Title"
                variant="outlined"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
            />
            <FormControl required>
                <InputLabel id="asset-type-label">Asset Type</InputLabel>
                <Select
                    labelId="asset-type-label"
                    value={assetType}
                    label="Asset Type"
                    onChange={(e) => setAssetType(e.target.value)}
                >
                    {assetTypes.map(type => (
                        <MenuItem key={type} value={type}>{type.replace('_', ' ')}</MenuItem>
                    ))}
                </Select>
            </FormControl>
            <Button
                variant="contained"
                component="label"
            >
                {file ? `Selected: ${file.name}` : 'Select Audio File'}
                <input
                    id="audio-file-input"
                    type="file"
                    hidden
                    accept="audio/*"
                    onChange={handleFileChange}
                />
            </Button>
            <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={isLoading}
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