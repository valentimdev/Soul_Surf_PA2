// src/api/services/authService.ts
import api from "../axios";
import { authRoutes } from "../routes/auth";

// DTOs do backend
export type LoginRequest = {
  email: string;
  password: string;
};

export type SignupRequest = {
  email: string;
  password: string;
};

export type ForgotPasswordRequest = {
  email: string;
};

export type ResetPasswordRequest = {
  token: string;
  newPassword: string;
};

export type JwtResponse = {
  token: string;
  type: string; // geralmente "Bearer"
};

export type MessageResponse = {
  message: string;
};

export const AuthService = {
  login: async (payload: LoginRequest): Promise<JwtResponse> => {
    const { data } = await api.post<JwtResponse>(authRoutes.login(), payload);
    // Armazenar token no localStorage se necessário
    if (data.token) {
      localStorage.setItem("token", data.token);
    }
    return data;
  },

  signup: async (payload: SignupRequest): Promise<MessageResponse> => {
    const { data } = await api.post<MessageResponse>(authRoutes.signup(), payload);
    return data;
  },

  forgotPassword: async (payload: ForgotPasswordRequest): Promise<MessageResponse> => {
    const { data } = await api.post<MessageResponse>(authRoutes.forgotPassword(), payload);
    return data;
  },

  resetPassword: async (payload: ResetPasswordRequest): Promise<MessageResponse> => {
    const { data } = await api.post<MessageResponse>(authRoutes.resetPassword(), payload);
    return data;
  },

  logout: (): void => {
    localStorage.removeItem("token");
  },
};