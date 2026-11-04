import axios from 'axios'

const gatewayUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')
const api = axios.create({ baseURL: `${gatewayUrl}/api`, headers: { 'Content-Type': 'application/json' }, timeout: 10000 })
export default api
