
import api from "../axios";
import { authRoutes } from "../routes/auth";

// Tipos de DTO (padrão com base no seu backend)
export type LoginRequest = {
  email: string;
  senha: string;
};

export type SignupRequest = {
  email: string;
  senha: string;
  nome: string;
};

export type ForgotPasswordRequest = {
  email: string;
};

export type ResetPasswordRequest = {
  token: string;
  newPassword: string;
};

export const AuthService = {
  login: async (payload: LoginRequest) => {
    const { data } = await api.post(authRoutes.login(), payload);
    // backend retorna { token }
    if (data.token) {
      localStorage.setItem("token", data.token);
    }
    return data;
  },

  signup: async (payload: SignupRequest) => {
    const { data } = await api.post(authRoutes.signup(), payload);
    return data;
  },

  forgotPassword: async (payload: ForgotPasswordRequest) => {
    const { data } = await api.post(authRoutes.forgotPassword(), payload);
    return data;
  },

  resetPassword: async (payload: ResetPasswordRequest) => {
    const { data } = await api.post(authRoutes.resetPassword(), payload);
    return data;
  },

  logout: () => {
    localStorage.removeItem("token");
  },
};
