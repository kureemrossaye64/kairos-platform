// src/pages/FacilitiesPage.js (New file)
import React, { useState, useEffect } from 'react';
import { Paper, Typography, Box, List, ListItem, ListItemText, Button, Divider, CircularProgress, Alert } from '@mui/material';

// We'll use hardcoded synthetic data here to match the backend for speed.
// A real app would fetch this from a `/api/v1/facilities` endpoint.
import { getAllFacilities } from '../services/api';

export default function FacilitiesPage() {

    const [facilities, setFacilities] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchFacilities = async () => {
            try {
                setLoading(true);
                setError('');
                const data = await getAllFacilities();
                setFacilities(data);
            } catch (err) {
                console.error("Failed to fetch facilities:", err);
                setError('Failed to load facilities. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        fetchFacilities();
    }, []);

    const handleBook = (facilityName) => {
        alert(`Booking functionality for ${facilityName} would be implemented here.`);
    };

    const renderContent = () => {
        if (loading) {
            return (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                </Box>
            );
        }

        if (error) {
            return <Alert severity="error">{error}</Alert>;
        }

        if (facilities.length === 0) {
            return <Alert severity="info">No facilities found in the system.</Alert>;
        }

        return (
            <List>
                {facilities.map((facility, index) => (
                    <React.Fragment key={facility.id}>
                        <ListItem
                            secondaryAction={
                                <Button variant="contained" onClick={() => handleBook(facility.name)}>
                                    Book
                                </Button>
                            }
                        >
                            <ListItemText
                                primary={facility.name}
                                secondary={`${facility.type} - ${facility.location}`}
                            />
                        </ListItem>
                        {index < facilities.length - 1 && <Divider />}
                    </React.Fragment>
                ))}
            </List>
        );
    };

    return (
        <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
                Sports Facilities Registry
            </Typography>
            {renderContent()}
        </Paper>
    );
}