import React from 'react';
import { Paper, BottomNavigation, BottomNavigationAction } from '@mui/material';
import { NavLink, useLocation } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import TravelExploreIcon from '@mui/icons-material/TravelExplore';
import FactCheckIcon from '@mui/icons-material/FactCheck';

// A helper mapping routes to tab values
const routeMap = {
    '/chat': 0,
    '/ingestion': 1,
    '/crawler': 2,
    '/review': 3,
};

export default function BottomNavBar() {
    const location = useLocation();
    // Determine the current value based on the current URL path
    const currentValue = routeMap[location.pathname];

    return (
        // Paper provides elevation and a background.
        // The position 'fixed' with bottom, left, right set to 0 sticks it to the bottom.
        <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1100 }} elevation={3}>
            <BottomNavigation
                showLabels
                value={currentValue}
            >
                <BottomNavigationAction
                    label="Kaya"
                    icon={<DashboardIcon />}
                    component={NavLink}
                    to="/chat"
                />
                <BottomNavigationAction
                    label="Ingestion"
                    icon={<CloudUploadIcon />}
                    component={NavLink}
                    to="/ingestion"
                />
                <BottomNavigationAction
                    label="Crawler"
                    icon={<TravelExploreIcon />}
                    component={NavLink}
                    to="/crawler"
                />
                <BottomNavigationAction
                    label="Review"
                    icon={<FactCheckIcon />}
                    component={NavLink}
                    to="/review"
                />
            </BottomNavigation>
        </Paper>
    );
}