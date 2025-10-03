import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box, CssBaseline, Toolbar, useMediaQuery, useTheme } from '@mui/material';
import DesktopSidebar from './common/DesktopSidebar';
import BottomNavBar from './common/BottomNavBar';

export default function AppLayout() {
    const theme = useTheme();
    // useMediaQuery is a hook that returns true if the screen matches the media query.
    // 'md' is the breakpoint for medium screens (desktops), typically 900px.
    const isDesktop = useMediaQuery(theme.breakpoints.up('md'));

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            
            {/*
              The core responsive logic:
              - If the screen is desktop-sized, render the sidebar.
              - If the screen is smaller, render the bottom navigation bar.
            */}
            {isDesktop ? <DesktopSidebar /> : <BottomNavBar />}

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    // Add padding to the bottom on mobile to prevent content
                    // from being hidden by the fixed bottom navigation bar.
                    pb: isDesktop ? 3 : '72px' // 56px (navbar height) + 16px padding
                }}
            >
                {/* 
                  The Toolbar is only needed as a spacer for the desktop view's top app bar area.
                  We don't need a spacer for the mobile view's top, but we need one at the bottom.
                */}
                {isDesktop && <Toolbar />}
                
                {/* Child routes (our pages) will render here, regardless of layout */}
                <Outlet />
            </Box>
        </Box>
    );
}