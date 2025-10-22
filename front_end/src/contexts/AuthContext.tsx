import { createContext, useContext, useEffect, useState } from "react";
import api from "@/api/axios";

interface AuthContextType {
    isAuthenticated: boolean;
    token: string | null;
    login: (token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    token: null,
    login: () => {},
    logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem("token"));
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!token);

    useEffect(() => {
        if (token) {
            api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
            setIsAuthenticated(true);
        } else {
            delete api.defaults.headers.common["Authorization"];
            setIsAuthenticated(false);
        }
    }, [token]);

    const login = (newToken: string) => {
        localStorage.setItem("token", newToken);
        setToken(newToken);
        setIsAuthenticated(true);
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
