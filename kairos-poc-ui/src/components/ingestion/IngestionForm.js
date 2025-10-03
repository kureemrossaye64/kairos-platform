import React, { useState } from 'react';
import { Box, Button, CircularProgress, Alert, Paper, Typography } from '@mui/material';
import FileUpload from '../common/FileUpload';
import MetadataForm from './MetadataForm';
import { ingestKnowledgePacket } from '../../services/api'; // We will create this API function

export default function IngestionForm() {
    const [file, setFile] = useState(null);
    const [metadata, setMetadata] = useState([{ key: '', value: '' }]);
    const [isLoading, setIsLoading] = useState(false);
    const [feedback, setFeedback] = useState({ type: '', message: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            setFeedback({ type: 'error', message: 'Please select a file to upload.' });
            return;
        }

        setIsLoading(true);
        setFeedback({ type: '', message: '' });

        // Convert the metadata array to the simple {key: value} object the backend expects
        const manifest = metadata.reduce((acc, curr) => {
            if (curr.key) { // Only include rows with a key
                acc[curr.key] = curr.value;
            }
            return acc;
        }, {});

        try {
            await ingestKnowledgePacket(file, manifest);
            setFeedback({ type: 'success', message: 'Knowledge Packet accepted! It will be processed and indexed in the background.' });
            // Reset form
            setFile(null);
            setMetadata([{ key: '', value: '' }]);
        } catch (error) {
            console.error("Error ingesting packet:", error);
            setFeedback({ type: 'error', message: error.response?.data || 'Ingestion failed. Please check the logs.' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>Ingest New Knowledge</Typography>
            <Typography color="textSecondary" sx={{mb: 3}}>
                Upload a document, audio, video, or image file. Add optional metadata to make the information more searchable and context-rich for the AI.
            </Typography>
            <form onSubmit={handleSubmit}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                    <FileUpload onFileSelect={setFile} selectedFile={file} />
                    <MetadataForm metadata={metadata} setMetadata={setMetadata} />
                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        disabled={isLoading || !file}
                        sx={{ mt: 2, alignSelf: 'flex-start' }}
                    >
                        {isLoading ? <CircularProgress size={24} /> : 'Submit for Ingestion'}
                    </Button>
                    {feedback.message && (
                        <Alert severity={feedback.type} sx={{ mt: 2 }}>{feedback.message}</Alert>
                    )}
                </Box>
            </form>
        </Paper>
    );
}