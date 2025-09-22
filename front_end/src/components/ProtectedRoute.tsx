import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext.tsx";

export default function ProtectedRoute() {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />; // renderiza as rotas filhas normalmente
}