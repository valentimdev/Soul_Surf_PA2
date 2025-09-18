// src/api/axios.ts
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api", // 👈 ajuste porta se necessário
  withCredentials: false, // Spring Boot geralmente usa JWT no header, não cookies
});

// Request interceptor -> adiciona token JWT se existir
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor -> pode capturar erros globais
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // exemplo: se 401, redirecionar para login
    if (error.response?.status === 401) {
      console.warn("Token inválido ou expirado. Faça login novamente.");
      // window.location.href = "/login"; // se quiser redirecionar automático
    }
    return Promise.reject(error);
  }
);

export default api;
