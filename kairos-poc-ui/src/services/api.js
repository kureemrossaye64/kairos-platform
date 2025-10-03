
import axios from 'axios';

const HARDCODED_JWT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwb2MtdXNlciIsImlhdCI6MTc1NzE2ODMxNiwiZXhwIjoyNjIxMTY4MzE2fQ.Fc9ULwZaLuYCQBFhMAHeYjpEq_cec3KWnRxFEXWkVxhwWiKF3LNhop-dZURl1a87";

export const nexusApi = axios.create({
    baseURL: 'http://localhost:8085/api/v1' // Points to the sports-atlas-app
});

// const addAuthInterceptor = (instance) => {
//     instance.interceptors.request.use(config => {
//         if (HARDCODED_JWT_TOKEN && !HARDCODED_JWT_TOKEN.startsWith("placeholder")) {
//             config.headers.Authorization = `Bearer ${HARDCODED_JWT_TOKEN}`;
//         }
//         return config;
//     });
// };
// addAuthInterceptor(nexusApi);

export const loginUser = async (credentials) => {
    const response = await nexusApi.post('/auth/login', credentials);
    return response.data;
};

export const registerUser = async (userData) => {
    const response = await nexusApi.post('/auth/register', userData);
    return response.data;
};

// ====================================================================
//                       API FUNCTION EXPORTS
// ====================================================================

// --- Cultural Archive ---

export const chatWithAgent = async (message) => {
    const response = await nexusApi.post('/atlas/chat', { message });
    return response.data;
};



export const ingestKnowledgePacket = (file, manifest) => {
    const formData = new FormData();
    formData.append('file', file);
    // The backend expects the manifest as a JSON string
    formData.append('manifest', JSON.stringify(manifest));
    return nexusApi.post('/knowledge/ingest', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

export const getAllFacilities = async () => {
    const response = await nexusApi.get('/facilities');
    return response.data;
};

export const getCrawlJobs = async () => {
    const response = await nexusApi.get('/crawler/jobs');
    return response.data;
};

export const createCrawlJob = async (jobData) => {
    const response = await nexusApi.post('/crawler/jobs', jobData);
    return response.data;
};

export const getPendingReviewItems = async () => {
    const response = await nexusApi.get('/review/pending');
    return response.data;
};

export const approveReviewItem = async (type, id) => {
    return nexusApi.post(`/review/approve/${type}/${id}`);
};