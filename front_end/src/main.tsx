import React from "react";
import ReactDOM from "react-dom/client";
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
import PostCommentsPage from "@/pages/PostCommentsPage.tsx";

// + importe a página de chat
import ChatPage from "./pages/ChatPage";
 // ou "@/pages/ChatPage" se usar alias

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    children: [
      { index: true, element: <LandingPage /> },

      {
        element: <ProtectedRoute />,
        children: [
          { path: "home", element: <App /> }, // antes era index
          { path: "perfil", element: <ProfilePage /> },
          { path: "registros", element: <NovoRegistroPage /> },
          { path: "praias", element: <BeachsPage /> },
          { path: "praias/:id", element: <BeachDetailPage /> },
          { path: "posts/:id/comments", element: <PostCommentsPage /> },

          { path: "chat/:conversationId", element: <ChatPage /> },
        ],
      },

      { path: "login", element: <LoginPage /> },
      { path: "cadastro", element: <CadastroPage /> },
      { path: "forgot-password", element: <ForgotPasswordPage /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <AuthProvider>
            <RouterProvider router={router} />
        </AuthProvider>
    </React.StrictMode>
);