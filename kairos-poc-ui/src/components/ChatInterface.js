// src/components/ChatInterface.js
import React, { useState, useRef, useEffect } from 'react';
import { Box, TextField, Button, Paper, Typography, CircularProgress, IconButton } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';

export default function ChatInterface({ onSendMessage }) {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(scrollToBottom, [messages]);

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMessage = { sender: 'user', text: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);

        try {
            const aiResponseText = await onSendMessage(input);
            const aiMessage = { sender: 'ai', text: aiResponseText };
            setMessages(prev => [...prev, aiMessage]);
        } catch (error) {
            console.error("Error sending message:", error);
            const errorMessage = { sender: 'ai', text: 'Sorry, I encountered an error. Please try again.' };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '70vh' }}>
            <Paper elevation={2} sx={{ flexGrow: 1, p: 2, overflowY: 'auto', mb: 2 }}>
                {messages.length === 0 ? (
                    <Typography color="textSecondary" align="center" sx={{height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
                        Ask a question to start the conversation.
                    </Typography>
                ) : (
                    messages.map((msg, index) => (
                        <Box key={index} sx={{
                            mb: 2,
                            display: 'flex',
                            justifyContent: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                        }}>
                            <Paper
                                variant="outlined"
                                sx={{
                                    p: 1.5,
                                    bgcolor: msg.sender === 'user' ? 'primary.light' : 'grey.200',
                                    color: msg.sender === 'user' ? 'primary.contrastText' : 'text.primary',
                                    maxWidth: '70%',
                                    whiteSpace: 'pre-wrap', // To respect newlines in the response
                                }}
                            >
                                {msg.text}
                            </Paper>
                        </Box>
                    ))
                )}
                {isLoading && (
                     <Box sx={{ display: 'flex', justifyContent: 'flex-start' }}>
                        <CircularProgress size={24} />
                    </Box>
                )}
                <div ref={messagesEndRef} />
            </Paper>
            <Box sx={{ display: 'flex' }}>
                <TextField
                    fullWidth
                    variant="outlined"
                    placeholder="Type your message..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    disabled={isLoading}
                    multiline
                    maxRows={4}
                />
                <IconButton color="primary" onClick={handleSend} disabled={isLoading} sx={{ml: 1}}>
                    <SendIcon />
                </IconButton>
            </Box>
        </Box>
    );
}