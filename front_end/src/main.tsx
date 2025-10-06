import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

import RootLayout from './layouts/RootLayout';
import App from './App';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import CadastroPage from './pages/CadastroPage';
import ProfilePage from './pages/ProfilePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage.tsx';
import NovoRegistroPage from './pages/NovoRegistroPage.tsx';
import ProtectedRoute from "./components/ProtectedRoute";
import './index.css';
import BeachsPage from './pages/BeachsPage.tsx';
import { AuthProvider } from './contexts/AuthContext.tsx';
import BeachDetailPage from "@/pages/BeachDetailPage.tsx";

const router = createBrowserRouter([
    {
        path: "/",
        element: <RootLayout />,
        children: [
            // rota pública padrão → Landing
            { index: true, element: <LandingPage /> },

            // rotas protegidas
            {
                element: <ProtectedRoute />,
                children: [
                    { path: "home", element: <App /> }, // antes era index
                    { path: "perfil", element: <ProfilePage /> },
                    { path: "registros", element: <NovoRegistroPage /> },
                    { path: "praias", element: <BeachsPage /> },
                    { path: "praias/:id", element: <BeachDetailPage /> },
                ],
            },

            // outras rotas públicas
            { path: "login", element: <LoginPage /> },
            { path: "cadastro", element: <CadastroPage /> },
            { path: "forgot-password", element: <ForgotPasswordPage /> },
        ],
    },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
  </StrictMode>
);