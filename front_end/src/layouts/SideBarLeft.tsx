// src/components/Sidebar.tsx

import React from 'react';
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
  isActive?: boolean; // Para destacar o item ativo
}

const navItems: NavItemProps[] = [
  { href: '/', icon: Home, label: 'Início', isActive: true },
  { href: '/perfil', icon: User, label: 'Perfil' },
  { href: '/', icon: Compass, label: 'Explorar' },
  { href: '/', icon: Waves, label: 'Praias' },
  { href: '/', icon: Users, label: 'Grupos' },
  { href: '/', icon: MessageSquare, label: 'Mensagens' },
  { href: '/', icon: Bell, label: 'Notificações' },
];

const SidebarLeft: React.FC = () => {
  return (
    <aside className="w-80 h-[90%] flex flex-col justify-between p-4 bg-white border-r border-gray-200">
      {/* Seção Principal de Navegação */}
      <nav>
        <ul className="space-y-1">
          {navItems.map((item) => (
            <li key={item.label}>
              <a href={item.href}>
                <Button
                  variant={item.isActive ? 'secondary' : 'ghost'}
                  className="w-full justify-start text-md py-5"
                >
                  <item.icon className="mr-3 h-5 w-5" />
                  {item.label}
                </Button>
              </a>
            </li>
          ))}
        </ul>
      </nav>

      {/* Seção Inferior: Perfil e Configurações */}
      <div>
        <Button
          variant="ghost"
          className="w-full justify-start text-md py-6 mb-2"
        >
          <Settings className="mr-3 h-5 w-5" />
          Configurações
        </Button>
      </div>
    </aside>
  );
};

export default SidebarLeft;