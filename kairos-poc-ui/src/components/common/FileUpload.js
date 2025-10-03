import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';

export default function FileUpload({ onFileSelect, selectedFile, accept }) {
    return (
        <Box
            sx={{
                border: '2px dashed',
                borderColor: 'grey.400',
                borderRadius: 1,
                p: 3,
                textAlign: 'center',
                backgroundColor: 'grey.50',
                cursor: 'pointer'
            }}
            component="label" // This makes the whole box clickable for the file input
            htmlFor="file-upload-input"
        >
            <UploadFileIcon sx={{ fontSize: 48, color: 'grey.500' }} />
            <Typography variant="h6" gutterBottom>
                Click or drag file to this area to upload
            </Typography>
            {selectedFile ? (
                <Typography variant="body1" color="primary">
                    Selected: {selectedFile.name}
                </Typography>
            ) : (
                <Typography variant="body2" color="textSecondary">
                    Supports all major document, audio, video, and image formats.
                </Typography>
            )}
            <input
                id="file-upload-input"
                type="file"
                hidden
                accept={accept || '*/*'} // Allow any file type unless specified
                onChange={(e) => onFileSelect(e.target.files[0])}
            />
        </Box>
    );
}