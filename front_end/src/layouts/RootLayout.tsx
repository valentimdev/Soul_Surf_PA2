import React, { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import {ArrowLeft, Plus} from 'lucide-react';

// Componentes existentes
import Header from '@/layouts/Header';
import SideBarLeft from '@/layouts/SideBarLeft';
import NovoRegistroCard from '@/components/customCards/NovoRegistroCard'; // Importa o seu card

// Componentes de UI da Shadcn
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogTrigger,
} from "@/components/ui/dialog";

const RootLayout: React.FC = () => {
  const location = useLocation();
  // Estado para controlar a abertura/fecho do modal
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Rotas onde o layout principal (com sidebars e botão) não deve aparecer
  const noLayoutRoutes = [
    '/',
    '/login',
    '/cadastro',
    '/forgot-password',
    '/landing',
  ];
  const showLayout = !noLayoutRoutes.includes(location.pathname);
  const goBack = () => window.history.back();

  return (
    <main>
      {showLayout ? (
        // Layout principal da aplicação (para utilizadores logados)
        <>
          <div className="fixed top-0 left-0 right-0 z-40"> {/* z-index ajustado */}
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
                      className="absolute top-2 left-2 flex items-center gap-1 bg-white text-black p-2 rounded-full shadow z-50"
                  >
                      <ArrowLeft className="w-5 h-5 text-[#5899c2]" />
                      <span className="hidden sm:inline">Voltar</span>
                  </Button>
                  )}
                  <Outlet />
              </div>

              <div className="hidden md:block w-[20%]">
              {/* <SideBarRight /> */}
            </div>
          </div>

          {/* DIALOG E BOTÃO FLUTUANTE (FAB) */}
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
                  <NovoRegistroCard
                      onSuccess={() => {
                          setIsModalOpen(false);
                      }}
                  />
              </DialogContent>
          </Dialog>
        </>
      ) : (
        // Layout para páginas públicas (login, cadastro, etc.)
        <div className="w-full">
          <Outlet />
        </div>
      )}
    </main>
  );
};

export default RootLayout;