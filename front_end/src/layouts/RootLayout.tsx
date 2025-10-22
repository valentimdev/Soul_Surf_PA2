import React, { useState } from 'react';
import { Outlet, useLocation, Navigate } from 'react-router-dom';
import {ArrowLeft, Plus} from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';

import Header from '@/layouts/Header';
import SideBarLeft from '@/layouts/SideBarLeft';
import NovoRegistroCard from '@/components/customCards/NovoRegistroCard';

import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";

const RootLayout: React.FC = () => {
    const location = useLocation();
    const { isAuthenticated } = useAuth();

    const [isModalOpen, setIsModalOpen] = useState(false);

    const noLayoutRoutes = [
        '/',
        '/login',
        '/cadastro',
        '/forgot-password',
        '/landing',
    ];

    const showLayout = !noLayoutRoutes.includes(location.pathname);

    if (showLayout && !isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    const goBack = () => window.history.back();

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
                            {location.pathname !== "/home" && (
                                <Button
                                    onClick={goBack}
                                    className="absolute top-2 left-2 flex items-center gap-1 bg-white text-black p-2 rounded-full shadow z-10"
                                >
                                    <ArrowLeft className="w-5 h-5 text-[#5899c2]" />
                                    <span className="hidden sm:inline">Voltar</span>
                                </Button>
                            )}
                            <Outlet />
                        </div>

                        <div className="hidden md:block w-[20%]">
                        </div>
                    </div>

                    <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
                        <DialogTrigger asChild>
                            <Button
                                className="fixed bottom-8 right-8 h-16 w-16 rounded-full shadow-lg z-50"
                                size="icon"
                            >
                                <Plus className="h-8 w-8" />
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="sm:max-w-[650px] p-0">
                            <NovoRegistroCard onSuccess={() => setIsModalOpen(false)} />
                        </DialogContent>
                    </Dialog>
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
