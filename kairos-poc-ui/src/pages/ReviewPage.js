import React, { useState, useEffect } from 'react';
import { Paper, Typography, Box, List, ListItem, ListItemText, Button, Divider, CircularProgress, Alert } from '@mui/material';
import { getPendingReviewItems, approveReviewItem } from '../services/api';

export default function ReviewPage() {
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    
    const fetchItems = async () => {
        try {
            setIsLoading(true);
            const data = await getPendingReviewItems();
            setItems(data);
        } catch (err) {
            setError('Failed to fetch items for review.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchItems();
    }, []);

    const handleApprove = async (type, id) => {
        try {
            await approveReviewItem(type, id);
            // Refresh list after approval
            fetchItems();
        } catch (err) {
            setError(`Failed to approve item ${id}.`);
        }
    };

    return (
        <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>Review & Approval Queue</Typography>
            <Typography color="textSecondary" sx={{mb: 3}}>
                Approve new services submitted by partners and the community. Approved items will be made public and indexed for search.
            </Typography>
            {isLoading ? <CircularProgress /> : error ? <Alert severity="error">{error}</Alert> : items.length === 0 ? <Alert severity="info">The review queue is empty.</Alert> : (
                <List>
                    {items.map((item, index) => (
                        <React.Fragment key={item.id}>
                            <ListItem
                                secondaryAction={
                                    <Box>
                                        <Button variant="contained" color="success" sx={{mr: 1}} onClick={() => handleApprove(item.type, item.id)}>Approve</Button>
                                        <Button variant="outlined" color="error">Reject</Button>
                                    </Box>
                                }
                            >
                                <ListItemText
                                    primary={`${item.name} (${item.type})`}
                                    secondary={`Submitted: ${new Date(item.submittedAt).toLocaleString()}`}
                                />
                            </ListItem>
                            {index < items.length - 1 && <Divider />}
                        </React.Fragment>
                    ))}
                </List>
            )}
        </Paper>
    );
}