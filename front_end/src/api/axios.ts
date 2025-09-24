// src/api/axios.ts
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
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
    // exemplo: se 401, redirecionar para login
    if (error.response?.status === 401) {
      console.warn("Token inválido ou expirado. Faça login novamente.");
    }
    return Promise.reject(error);
  }
);

export default api;
