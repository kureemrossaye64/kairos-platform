import React from 'react';
import { NavLink } from 'react-router-dom';
import { Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography, Divider } from '@mui/material';
import BubbleChartIcon from '@mui/icons-material/BubbleChart';
import DashboardIcon from '@mui/icons-material/Dashboard';
import TravelExploreIcon from '@mui/icons-material/TravelExplore';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import FactCheckIcon from '@mui/icons-material/FactCheck';

const drawerWidth = 240;

const navLinkStyle = ({ isActive }) => ({
    textDecoration: 'none',
    color: 'inherit',
    backgroundColor: isActive ? 'rgba(0, 0, 0, 0.08)' : 'transparent',
    display: 'block'
});

export default function DesktopSidebar() {
    return (
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
                    KAIROS Nexus
                </Typography>
            </Toolbar>
            <Divider />
            <Box sx={{ overflow: 'auto' }}>
                <List>
                    <NavLink to="/chat" style={navLinkStyle}>
                        <ListItem disablePadding>
                            <ListItemButton>
                                <ListItemIcon><DashboardIcon /></ListItemIcon>
                                <ListItemText primary="Kaya Assistant" />
                            </ListItemButton>
                        </ListItem>
                    </NavLink>
                </List>
                <Divider />
                <Typography variant="overline" sx={{ pl: 2, pt: 2, display: 'block' }}>Admin Tools</Typography>
                <List>
                    <NavLink to="/ingestion" style={navLinkStyle}>
                        <ListItem disablePadding><ListItemButton><ListItemIcon><CloudUploadIcon /></ListItemIcon><ListItemText primary="Knowledge Ingestion" /></ListItemButton></ListItem>
                    </NavLink>
                    <NavLink to="/crawler" style={navLinkStyle}>
                        <ListItem disablePadding><ListItemButton><ListItemIcon><TravelExploreIcon /></ListItemIcon><ListItemText primary="Crawler Jobs" /></ListItemButton></ListItem>
                    </NavLink>
                    <NavLink to="/review" style={navLinkStyle}>
                        <ListItem disablePadding><ListItemButton><ListItemIcon><FactCheckIcon /></ListItemIcon><ListItemText primary="Review Queue" /></ListItemButton></ListItem>
                    </NavLink>
                </List>
            </Box>
        </Drawer>
    );
}