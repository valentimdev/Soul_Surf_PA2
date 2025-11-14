// src/api/axios.ts
import axios from "axios";

const api = axios.create({
  baseURL: "https://soulsurfpa2-production.up.railway.app/api",
  withCredentials: false,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  console.log(config.headers)
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.warn("Token inválido ou expirado. Faça login novamente.");
    }
    return Promise.reject(error);
  }
);

export default api;
