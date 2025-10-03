import React, { useState, useEffect } from 'react';
import { Box, Paper, Typography, Button, TextField, CircularProgress, Alert, List, ListItem, ListItemText, Divider, Chip } from '@mui/material';
import { getCrawlJobs, createCrawlJob } from '../services/api';

export default function CrawlerPage() {
    const [jobs, setJobs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    
    // Form state
    const [jobName, setJobName] = useState('');
    const [seedUrls, setSeedUrls] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    
    const fetchJobs = async () => {
        try {
            setIsLoading(true);
            const data = await getCrawlJobs();
            setJobs(data);
        } catch (err) {
            setError('Failed to fetch crawl jobs.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchJobs();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const urls = seedUrls.split('\n').map(url => url.trim()).filter(Boolean);
            await createCrawlJob({ name: jobName, seedUrls: urls, maxDepth: 2 });
            // Reset form and refetch jobs
            setJobName('');
            setSeedUrls('');
            fetchJobs();
        } catch (err) {
            setError('Failed to create job.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
            <Paper elevation={3} sx={{ p: 3, flex: 1 }}>
                <Typography variant="h5" gutterBottom>Create New Crawl Job</Typography>
                <form onSubmit={handleSubmit}>
                    <TextField label="Job Name" value={jobName} onChange={e => setJobName(e.target.value)} fullWidth required sx={{ mb: 2 }} />
                    <TextField label="Seed URLs (one per line)" value={seedUrls} onChange={e => setSeedUrls(e.target.value)} fullWidth required multiline rows={4} sx={{ mb: 2 }} />
                    <Button type="submit" variant="contained" disabled={isSubmitting}>
                        {isSubmitting ? <CircularProgress size={24} /> : 'Create and Schedule'}
                    </Button>
                </form>
            </Paper>
            <Paper elevation={3} sx={{ p: 3, flex: 2 }}>
                <Typography variant="h5" gutterBottom>Crawl Job History</Typography>
                {isLoading ? <CircularProgress /> : error ? <Alert severity="error">{error}</Alert> : (
                    <List>
                        {jobs.map((job, index) => (
                            <React.Fragment key={job.id}>
                                <ListItem>
                                    <ListItemText
                                        primary={job.name}
                                        secondary={`Status: ${job.status} | Last Run: ${job.lastRun ? new Date(job.lastRun).toLocaleString() : 'Never'}`}
                                    />
                                    <Chip label={`${job.seedUrls.length} seeds`} size="small" />
                                </ListItem>
                                {index < jobs.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                )}
            </Paper>
        </Box>
    );
}