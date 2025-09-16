import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Importe o layout e TODAS as páginas
import RootLayout from './layouts/RootLayout';
import App from './App';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import CadastroPage from './pages/CadastroPage';
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage.tsx';
import NovoRegistroPage from './pages/NovoRegistroPage.tsx';

import './index.css'; // O seu CSS global

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      {
        index: true,
        element: <App />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'landing',
        element: <LandingPage />
      },
      {
        path: 'cadastro',
        element: <CadastroPage />,
      },
      {
        path: 'perfil',
        element: <ProfilePage />,
      },
      {
        path: 'esqueci-a-senha',
        element: <ForgotPasswordPage />,
      },
      // Adicionando a nova rota aqui
      {
        path: 'registros/novo',
        element: <NovoRegistroPage />,
      },
    ],
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);