import React from 'react';
import { Paper, Typography } from '@mui/material';
import ChatInterface from '../components/chat/ChatInterface';
import { chatWithAgent } from '../services/api';

export default function KayaAssistantPage() {
    return (
        <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
                Kaya, Your National Youth & Community Governor
            </Typography>
            <Typography color="textSecondary" sx={{mb: 3}}>
                Ask me anything about sports, training opportunities, community events, or how to get involved in Mauritius.
            </Typography>
            <ChatInterface onSendMessage={chatWithAgent} />
        </Paper>
    );
}