export const authRoutes = {
  base: "/auth",

  login: () => `${authRoutes.base}/login`,
  signup: () => `${authRoutes.base}/signup`,
  forgotPassword: () => `${authRoutes.base}/forgot-password`,
  resetPassword: () => `${authRoutes.base}/reset-password`,
};
