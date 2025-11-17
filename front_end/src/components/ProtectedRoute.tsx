import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import LoadingSpinner from "@/components/LoadingSpinner.tsx";

export default function ProtectedRoute() {
    const { isAuthenticated, loading } = useAuth();

    if (loading) return <LoadingSpinner />;
    return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}
