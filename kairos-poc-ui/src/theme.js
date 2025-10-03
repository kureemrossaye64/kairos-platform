import { createTheme } from '@mui/material/styles';

// Create a base theme instance.
let theme = createTheme({
    // You can define your color palette, spacing, etc. here
    palette: {
        primary: {
            main: '#1976d2', // A standard blue
        },
        secondary: {
            main: '#dc004e', // A standard pink
        },
    },
});

// Now, we make the typography responsive.
// This overrides the default typography settings.
theme = createTheme(theme, {
    typography: {
        // For the main title: "Kaya, Your National Youth..."
        h5: {
            fontWeight: 500,
            fontSize: '1.5rem', // Default size for desktop
            [theme.breakpoints.down('md')]: { // For screens smaller than medium (tablets)
                fontSize: '1.3rem',
            },
            [theme.breakpoints.down('sm')]: { // For screens smaller than small (phones)
                fontSize: '1.2rem',
            },
        },
        // For the subtitle: "Ask me anything..."
        body1: {
            fontSize: '1rem', // Default desktop size
            [theme.breakpoints.down('sm')]: {
                fontSize: '0.9rem',
            },
        },
        // We can also make other elements responsive if needed
        h6: {
            fontWeight: 500,
            fontSize: '1.25rem',
            [theme.breakpoints.down('sm')]: {
                fontSize: '1.1rem',
            },
        }
    },
});

export default theme;