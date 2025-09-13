// src/pages/SportsAtlasPage.js
import React, { useState } from 'react';
import { Box, Tabs, Tab, Paper, Typography } from '@mui/material';
import CsvIngestForm from '../components/CsvIngestForm';
import ChatInterface from '../components/ChatInterface';
import { chatWithAtlas } from '../services/api'; // Note: Importing the correct chat function

// The TabPanel helper component can be defined here again or moved to a shared utils file.
// For simplicity in a POC, defining it here is fine.
function TabPanel(props) {
    const { children, value, index, ...other } = props;
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`atlas-tabpanel-${index}`}
            aria-labelledby={`atlas-tab-${index}`}
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

export default function SportsAtlasPage() {
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    return (
        <Paper elevation={3}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="sports atlas tabs">
                    <Tab label="Ingest Performance Data" id="atlas-tab-0" />
                    <Tab label="Chat with the Atlas" id="atlas-tab-1" />
                </Tabs>
            </Box>
            <TabPanel value={tabValue} index={0}>
                <Typography variant="h5" gutterBottom>
                    Add Performance Data
                </Typography>
                <Typography color="textSecondary" sx={{mb: 3}}>
                    Upload a CSV file with competition results. The system will parse the file and update the athlete database.
                    <br/>
                    <strong>Required Columns:</strong> Name, DateOfBirth, Sport, Event, Result, Unit, EventDate
                </Typography>
                <CsvIngestForm />
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
                 {/* 
                   THE POWER OF REUSABILITY:
                   We use the exact same ChatInterface component, but pass it a
                   different API function ('chatWithAtlas'). The component itself
                   doesn't need to know anything about the backend it's talking to.
                 */}
                <ChatInterface onSendMessage={chatWithAtlas} />
            </TabPanel>
        </Paper>
    );
}