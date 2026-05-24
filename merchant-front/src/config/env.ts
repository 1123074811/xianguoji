const DEFAULT_SERVER_HOST = import.meta.env.VITE_SERVER_HOST || '127.0.0.1';
const DEFAULT_SERVER_PORT = import.meta.env.VITE_SERVER_PORT || '8080';
const DEFAULT_API_PROTOCOL = import.meta.env.VITE_API_PROTOCOL || 'http';
const DEFAULT_WS_PROTOCOL = import.meta.env.VITE_WS_PROTOCOL || 'ws';

export const API_BASE_URL = import.meta.env.VITE_API_BASE
  || `${DEFAULT_API_PROTOCOL}://${DEFAULT_SERVER_HOST}:${DEFAULT_SERVER_PORT}`;

export const WS_BASE_URL = import.meta.env.VITE_WS_URL
  || getDefaultWsBaseUrl();

function getDefaultWsBaseUrl() {
  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${window.location.host}`;
  }
  return `${DEFAULT_WS_PROTOCOL}://${DEFAULT_SERVER_HOST}:${DEFAULT_SERVER_PORT}`;
}
