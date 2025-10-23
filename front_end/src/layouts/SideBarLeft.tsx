// src/layouts/SideBarLeft.tsx (Versão Atualizada com Modal de Configurações)
import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

// Ícones existentes e novos para as opções de configuração
import {
  Home,
  Waves,
  Users,
  MessageSquare,
  Bell,
  User,        // Para Perfil / Editar Perfil
  Settings,    // Para Configurações (o próprio gatilho)
  ShieldCheck, // Exemplo: Para Mudar Senha
  LogOut,      // Para Sair
  Mail,        // Exemplo: Para Notificações (opcional, pode ser Bell)
} from 'lucide-react';

// Componentes de UI da Shadcn/ui
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Card, CardContent } from "@/components/ui/card";
// Removida a importação de Avatar, pois não é utilizada neste ficheiro
// import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

// Tipagem para os itens de navegação
interface NavItemProps {
  href: string;
  icon: React.ElementType;
  label: string;
}

const navItems: NavItemProps[] = [
  { href: '/home', icon: Home, label: 'Início' },
  { href: '/perfil', icon: User, label: 'Perfil' },
  { href: '/praias', icon: Waves, label: 'Praias' },
  { href: '/grupos', icon: Users, label: 'Grupos' },
  { href: '/mensagens', icon: MessageSquare, label: 'Mensagens' },
  { href: '/notificacoes', icon: Bell, label: 'Notificações' },
];

const SidebarLeft: React.FC = () => {
  const location = useLocation();
  const [isSettingsOpen, setIsSettingsOpen] = useState(false); // Estado para controlar o modal

  // Função para lidar com o logout
  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/'; // Redireciona para a página inicial
  };

  return (
    <aside className="w-60 h-[90%] flex flex-col justify-between p-4 bg-white border-r border-gray-200">
      {/* Seção Principal de Navegação */}
      <nav>
        <ul className="space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.href;
            return (
              <li key={item.label}>
                <Link to={item.href}>
                  <Button
                    variant={isActive ? 'secondary' : 'ghost'}
                    className="w-full justify-start text-md py-5 hover:cursor-pointer"
                  >
                    <item.icon className="mr-3 h-5 w-5" />
                    {item.label}
                  </Button>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* PARTE DE BAIXO: CONFIGURAÇÕES E SAIR (AGORA NO MODAL) */}
      <div>
        <Dialog open={isSettingsOpen} onOpenChange={setIsSettingsOpen}>
          <DialogTrigger asChild>
            {/* Botão "Configurações" que abre o modal */}
            <Button
              variant={
                location.pathname === '/configuracoes' || isSettingsOpen
                  ? 'secondary'
                  : 'ghost'
              }
              className="w-full justify-start text-md py-6 mb-2 hover:cursor-pointer"
            >
              <Settings className="mr-3 h-5 w-5" />
              Configurações
            </Button>
          </DialogTrigger>

          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Configurações</DialogTitle>
            </DialogHeader>
            <Card className="border-none shadow-none">
              <CardContent className="p-2 space-y-1">
                {/* Opção 1: Editar Perfil */}
                <Button 
                  variant="ghost" 
                  className="w-full justify-start p-4 text-md hover:cursor-pointer"
                  onClick={() => {
                    alert("A navegar para Editar Perfil...");
                    setIsSettingsOpen(false); // Fecha o modal após a ação
                  }}
                >
                  <User className="mr-3 h-5 w-5" />
                  Editar Perfil
                </Button>
                {/* Opção 2: Mudar Senha */}
                <Button 
                  variant="ghost" 
                  className="w-full justify-start p-4 text-md hover:cursor-pointer"
                  onClick={() => {
                    alert("A navegar para Mudar Senha...");
                    setIsSettingsOpen(false); // Fecha o modal após a ação
                  }}
                >
                  <ShieldCheck className="mr-3 h-5 w-5" />
                  Mudar Senha
                </Button>
                {/* Opção 3: Notificações */}
                <Button 
                  variant="ghost" 
                  className="w-full justify-start p-4 text-md hover:cursor-pointer"
                  onClick={() => {
                    alert("A navegar para Notificações...");
                    setIsSettingsOpen(false); // Fecha o modal após a ação
                  }}
                >
                  <Mail className="mr-3 h-5 w-5" /> {/* Usei Mail, mas pode ser Bell */}
                  Notificações
                </Button>
                {/* Opção 4: Sair (Logout) - com estilo distinto */}
                <Button 
                  variant="ghost" 
                  className="w-full justify-start p-4 text-md text-red-600 hover:text-red-700 hover:bg-red-50 hover:cursor-pointer"
                  onClick={handleLogout} // Chama a função de logout
                >
                  <LogOut className="mr-3 h-5 w-5" />
                  Sair
                </Button>
              </CardContent>
            </Card>
          </DialogContent>
        </Dialog>
      </div>
    </aside>
  );
};

export default SidebarLeft;