// src/components/AppLayout.js
import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography, CssBaseline } from '@mui/material';
import BubbleChartIcon from '@mui/icons-material/BubbleChart'; // Placeholder for KAIROS logo
import ArchiveIcon from '@mui/icons-material/Archive';
import SportsScoreIcon from '@mui/icons-material/SportsScore';

import LocationOnIcon from '@mui/icons-material/LocationOn'

const drawerWidth = 240;

// Basic styling for the NavLink
const navLinkStyle = ({ isActive }) => ({
    textDecoration: 'none',
    color: 'inherit',
    backgroundColor: isActive ? 'rgba(0, 0, 0, 0.08)' : 'transparent',
    display: 'block'
});

export default function AppLayout() {
    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
                }}
            >
                <Toolbar>
                    <BubbleChartIcon sx={{ mr: 1 }} />
                    <Typography variant="h6" noWrap>
                        KAIROS POC
                    </Typography>
                </Toolbar>
                <Box sx={{ overflow: 'auto' }}>
                    <List>
                        <NavLink to="/archive" style={navLinkStyle}>
                            <ListItem disablePadding>
                                <ListItemButton>
                                    <ListItemIcon><ArchiveIcon /></ListItemIcon>
                                    <ListItemText primary="Cultural Archive" />
                                </ListItemButton>
                            </ListItem>
                        </NavLink>
                        <NavLink to="/facilities" style={navLinkStyle}>
                            <ListItem disablePadding>
                                <ListItemButton>
                                    <ListItemIcon><LocationOnIcon /></ListItemIcon>
                                    <ListItemText primary="Facilities" />
                                </ListItemButton>
                            </ListItem>
                        </NavLink>
                        <NavLink to="/atlas" style={navLinkStyle}>
                            <ListItem disablePadding>
                                <ListItemButton>
                                    <ListItemIcon><SportsScoreIcon /></ListItemIcon>
                                    <ListItemText primary="Sports Atlas" />
                                </ListItemButton>
                            </ListItem>
                        </NavLink>
                    </List>
                </Box>
            </Drawer>
            <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                <Toolbar /> {/* This is a spacer to push content below the top app bar area */}
                <Outlet /> {/* Child routes will render here */}
            </Box>
        </Box>
    );
}