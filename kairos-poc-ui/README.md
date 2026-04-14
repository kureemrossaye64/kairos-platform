# KAIROS Proof of Concept UI

This is a React-based frontend application designed to demonstrate the capabilities of the KAIROS platform. It provides interfaces for AI chat, document ingestion, web crawling, and data review.

## Overview

The KAIROS UI interacts with the KAIROS backend APIs to provide a user-friendly experience for:
- **Kaya Assistant**: A conversational AI interface for interacting with the platform's agents.
- **Knowledge Ingestion**: Uploading files and managing the ingestion pipeline.
- **Web Crawler**: Scheduling and monitoring web crawling jobs.
- **Data Review**: Reviewing and approving processed data.

## Technologies

- **React**: Frontend framework.
- **Material UI**: Component library.
- **Axios**: For API communication.
- **Context API**: For state management (e.g., Auth).

## Setup and Installation

### Prerequisites
- Node.js (v18+)
- npm

### Installation

1. Navigate to the `kairos-poc-ui` directory:
   ```bash
   cd kairos-poc-ui
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure the API endpoint:
   Open `src/services/api.js` and update the `baseURL` to point to your running KAIROS backend instance (default: `http://localhost:8085/api/v1`).

### Running the App

To start the development server:
```bash
npm start
```
The application will be available at [http://localhost:3000](http://localhost:3000).

## Build for Production

To create an optimized production build:
```bash
npm run build
```
The output will be in the `build` directory.

## License

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
