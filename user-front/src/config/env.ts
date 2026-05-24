const DEFAULT_SERVER_HOST = import.meta.env.VITE_SERVER_HOST || '127.0.0.1';
const DEFAULT_SERVER_PORT = import.meta.env.VITE_SERVER_PORT || '8080';
const DEFAULT_API_PROTOCOL = import.meta.env.VITE_API_PROTOCOL || 'http';

export const API_BASE_URL = import.meta.env.VITE_API_BASE
  || `${DEFAULT_API_PROTOCOL}://${DEFAULT_SERVER_HOST}:${DEFAULT_SERVER_PORT}`;
