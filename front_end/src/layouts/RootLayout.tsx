import React, { useEffect, useState } from 'react';
import { Outlet, useLocation, Navigate, useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import api from '@/api/axios';
import { userRoutes } from '@/api/routes/user';
import Header from '@/layouts/Header';
import SideBarLeft from '@/layouts/SideBarLeft';
import NovoRegistroCard from '@/components/customCards/NovoRegistroCard';
import NovaPraiaCard from '@/components/customCards/NovaPraiaCard';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog';
import { useAuth } from '@/contexts/AuthContext';

type User = {
    id: number;
    username: string;
    email: string;
    admin: boolean;
};

const RootLayout: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { token, logout, loading: authLoading } = useAuth();
    const [user, setUser] = useState<User | null>(null);
    const [loadingUser, setLoadingUser] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const noLayoutRoutes = [
        '/',
        '/login',
        '/cadastro',
        '/forgot-password',
        '/landing',
        '/about',
    ];
    const showLayout = !noLayoutRoutes.includes(location.pathname);

    useEffect(() => {
        const fetchUser = async () => {
            if (!token) {
                setLoadingUser(false);
                return;
            }
            try {
                const res = await api.get(userRoutes.getMe());
                setUser(res.data);
            } catch {
                logout();
                navigate('/login', { replace: true });
            } finally {
                setLoadingUser(false);
            }
        };
        fetchUser();
    }, [token, logout, navigate]);

    if (showLayout && (loadingUser || authLoading)) return <div>Carregando...</div>;
    if (showLayout && !user && !authLoading) return <Navigate to="/login" replace />;

    const isPraiasPage = location.pathname === '/praias' || location.pathname === '/beaches';
    const isAdmin = user?.admin === true;

    return (
        <main>
            {showLayout ? (
                <>
                    <div className="fixed top-0 left-0 right-0 z-40">
                        <Header />
                    </div>
                    <div className="flex pt-20">
                        <div className="hidden md:block w-[20%]">
                            <div className="fixed w-[20%] h-screen">
                                <SideBarLeft />
                            </div>
                        </div>
                        <div className="w-full md:w-[60%] relative">
                            <Outlet />
                        </div>
                        <div className="hidden md:block w-[20%]" />
                    </div>
                    {(isPraiasPage && isAdmin) || !isPraiasPage ? (
                        <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
                            <DialogTrigger asChild>
                                <Button
                                    className="fixed bottom-8 right-8 h-16 w-16 rounded-full shadow-lg z-1"
                                    size="icon"
                                >
                                    <Plus className="h-8 w-8" />
                                </Button>
                            </DialogTrigger>
                            <DialogContent className="sm:max-w-[650px] p-0">
                                {isPraiasPage ? (
                                    <NovaPraiaCard onSuccess={() => setIsModalOpen(false)} />
                                ) : (
                                    <NovoRegistroCard onSuccess={() => setIsModalOpen(false)} />
                                )}
                            </DialogContent>
                        </Dialog>
                    ) : null}
                </>
            ) : (
                <div className="w-full">
                    <Outlet />
                </div>
            )}
        </main>
    );
};

export default RootLayout;
