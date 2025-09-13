// src/pages/CulturalArchivePage.js
import React, { useState } from 'react';
import { Box, Tabs, Tab, Paper, Typography } from '@mui/material';
import AudioIngestForm from '../components/AudioIngestForm';
import ChatInterface from '../components/ChatInterface';
import { chatWithArchive } from '../services/api';

function TabPanel(props) {
    const { children, value, index, ...other } = props;
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`archive-tabpanel-${index}`}
            aria-labelledby={`archive-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    {children}
                </Box>
            )}
        </div>
    );
}

export default function CulturalArchivePage() {
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    return (
        <Paper elevation={3}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="cultural archive tabs">
                    <Tab label="Ingest Audio" id="archive-tab-0" />
                    <Tab label="Chat with the Archive" id="archive-tab-1" />
                </Tabs>
            </Box>
            <TabPanel value={tabValue} index={0}>
                <Typography variant="h5" gutterBottom>
                    Add Knowledge to the Archive
                </Typography>
                <Typography color="textSecondary" sx={{mb: 3}}>
                    Upload an audio recording of an oral history, song, or story. The system will transcribe it and make it searchable for the chat agent.
                </Typography>
                <AudioIngestForm />
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
                 {/* Here we pass the specific API function to our reusable chat component */}
                <ChatInterface onSendMessage={chatWithArchive} />
            </TabPanel>
        </Paper>
    );
}