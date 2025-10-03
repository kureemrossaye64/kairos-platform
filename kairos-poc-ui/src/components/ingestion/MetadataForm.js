import React from 'react';
import { Box, TextField, IconButton, Typography, Grid, Button } from '@mui/material';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutline';

export default function MetadataForm({ metadata, setMetadata }) {
    const handleAddRow = () => {
        setMetadata([...metadata, { key: '', value: '' }]);
    };

    const handleRemoveRow = (index) => {
        const newMetadata = metadata.filter((_, i) => i !== index);
        setMetadata(newMetadata);
    };

    const handleInputChange = (index, event) => {
        const { name, value } = event.target;
        const newMetadata = [...metadata];
        newMetadata[index][name] = value;
        setMetadata(newMetadata);
    };

    return (
        <Box>
            <Typography variant="h6" gutterBottom>Metadata Manifest (Optional)</Typography>
            {metadata.map((row, index) => (
                <Grid container spacing={2} key={index} sx={{ mb: 1, alignItems: 'center' }}>
                    <Grid item xs={5}>
                        <TextField
                            name="key"
                            label="Key"
                            variant="outlined"
                            size="small"
                            fullWidth
                            value={row.key}
                            onChange={(e) => handleInputChange(index, e)}
                        />
                    </Grid>
                    <Grid item xs={5}>
                        <TextField
                            name="value"
                            label="Value"
                            variant="outlined"
                            size="small"
                            fullWidth
                            value={row.value}
                            onChange={(e) => handleInputChange(index, e)}
                        />
                    </Grid>
                    <Grid item xs={2}>
                        <IconButton onClick={() => handleRemoveRow(index)}>
                            <RemoveCircleOutlineIcon />
                        </IconButton>
                    </Grid>
                </Grid>
            ))}
            <Button
                startIcon={<AddCircleOutlineIcon />}
                onClick={handleAddRow}
            >
                Add Metadata Field
            </Button>
        </Box>
    );
}