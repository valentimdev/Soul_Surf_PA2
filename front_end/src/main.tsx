import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Importe o layout e as páginas
import RootLayout from './layouts/RootLayout';
import App from './App';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import CadastroPage from './pages/CadastroPage';
import HomePage from './pages/HomePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage'; // Sua página

import './index.css'; // Seu CSS global

// Crie a configuração das rotas
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
        path: 'home',
        element: <HomePage />,
      },
      {
        // Sua rota integrada corretamente com as outras
        path: 'esqueci-a-senha',
        element: <ForgotPasswordPage />,
      },
    ],
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);

