// src/components/Sidebar.tsx
import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  Home,
  Compass,
  Waves,
  Users,
  MessageSquare,
  Bell,
  User,
  Settings,
} from 'lucide-react';
import { Button } from '@/components/ui/button'; // Ajuste o caminho se necessário
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'; // Ajuste o caminho

// Tipagem para os itens de navegação
interface NavItemProps {
  href: string;
  icon: React.ElementType;
  label: string;
}

const navItems: NavItemProps[] = [
  { href: '/', icon: Home, label: 'Início' },
  { href: '/perfil', icon: User, label: 'Perfil' },
  { href: '/explorar', icon: Compass, label: 'Explorar' },
  { href: '/praias', icon: Waves, label: 'Praias' },
  { href: '/grupos', icon: Users, label: 'Grupos' },
  { href: '/mensagens', icon: MessageSquare, label: 'Mensagens' },
  { href: '/notificacoes', icon: Bell, label: 'Notificações' },
];

const SidebarLeft: React.FC = () => {
  const location = useLocation(); 

  return (
    <aside className="w-80 h-[90%] flex flex-col justify-between p-4 bg-white border-r border-gray-200">
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
                    className="w-full justify-start text-md py-5"
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

      {/* parte de baixo configurações */}
      <div>
        <Link to="/configuracoes">
          <Button
            variant={
              location.pathname === '/configuracoes' ? 'secondary' : 'ghost'
            }
            className="w-full justify-start text-md py-6 mb-2"
          >
            <Settings className="mr-3 h-5 w-5" />
            Configurações
          </Button>
        </Link>
      </div>
    </aside>
  );
};

export default SidebarLeft;
