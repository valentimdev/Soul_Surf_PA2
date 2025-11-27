import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
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
import BeachDetailPage from '@/pages/BeachDetailPage.tsx';
import PostCommentsPage from '@/pages/PostCommentsPage.tsx';
import AboutPage from './pages/AboutPage.tsx';
import ChatPage from './pages/ChatPage';
import UserTimelinePage from './pages/UserTimelinePage.tsx';
import SearchResultsPage from "@/pages/SearchResultsPage.tsx";
import ResetPasswordPage from './pages/ResetPasswordPage.tsx';

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
                    { path: 'perfil/:userId', element: <ProfilePage /> },
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
            { path: 'reset-password', element: <ResetPasswordPage /> },
        ],
    },
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
        <AuthProvider>
            <RouterProvider router={router} />
        </AuthProvider>
);
