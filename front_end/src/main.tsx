import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Toaster } from 'sonner';
import MessagesPage from './pages/MessagesPage';
import RootLayout from './layouts/RootLayout';
import App from './App';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import CadastroPage from './pages/CadastroPage';
import ProfilePage from './pages/ProfilePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage.tsx';
import NovoRegistroPage from './pages/NovoRegistroPage.tsx';
import ProtectedRoute from './components/ProtectedRoute';
import './index.css';
import BeachsPage from './pages/BeachsPage.tsx';
import { AuthProvider } from './contexts/AuthContext.tsx';
import { NotificationProvider } from './contexts/NotificationContext.tsx';
import BeachDetailPage from '@/pages/BeachDetailPage.tsx';
import PostCommentsPage from '@/pages/PostCommentsPage.tsx';
import AboutPage from './pages/AboutPage.tsx';
import ChatPage from './pages/ChatPage';
import UserTimelinePage from './pages/UserTimelinePage.tsx';
import SearchResultsPage from "@/pages/SearchResultsPage.tsx";

const router = createBrowserRouter([
    {
        path: '/',
        element: <RootLayout />,
        children: [
            { index: true, element: <LandingPage /> },

            {
                element: <ProtectedRoute />,
                children: [
                    { path: 'home', element: <App /> },
                    { path: 'perfil/:username', element: <ProfilePage /> },
                    { path: 'perfil', element: <ProfilePage /> },
                    { path: 'registros', element: <NovoRegistroPage /> },
                    { path: 'praias', element: <BeachsPage /> },
                    { path: 'praias/:id', element: <BeachDetailPage /> },
                    { path: 'posts/:id/comments', element: <PostCommentsPage /> },
                    { path: 'buscar', element: <SearchResultsPage /> },
                    { path: 'chat/:conversationId', element: <ChatPage /> },
                    { path: 'mensagens', element: <MessagesPage /> },
                    { path: 'usuarios', element: <UserTimelinePage /> },
                ],
            },

            { path: 'about', element: <AboutPage /> },
            { path: 'login', element: <LoginPage /> },
            { path: 'cadastro', element: <CadastroPage /> },
            { path: 'forgot-password', element: <ForgotPasswordPage /> },
        ],
    },
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
    <AuthProvider>
        <NotificationProvider>
            <Toaster
                position="top-right"
                richColors
                closeButton
                toastOptions={{
                    duration: 5000,
                    style: {
                        background: 'white',
                        border: '1px solid #e5e7eb',
                        boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)',
                    },
                }}
            />
            <RouterProvider router={router} />
        </NotificationProvider>
    </AuthProvider>
);
