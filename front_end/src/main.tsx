// src/main.tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Importe o layout e as páginas
import RootLayout from './layouts/RootLayout';
import App from './App';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';

import './index.css'; // Seu CSS global

// Crie a configuração das rotas
const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      {
        index: true,
        element: <App />, // Rota '/' renderiza o App
      },
      {
        path: 'login',
        element: <LoginPage />, // Rota '/login' renderiza a LoginPage
      },
      {
        path: 'landing',
        element: <LandingPage />
      }
    ],
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
