import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    Home,
    Waves,
    Users,
    MessageSquare,
    User,
    Settings,
    LogOut,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Card, CardContent } from "@/components/ui/card";

interface NavItemProps {
    href: string;
    icon: React.ElementType;
    label: string;
}

const navItems: NavItemProps[] = [
    { href: '/home', icon: Home, label: 'Início' },
    { href: '/perfil', icon: User, label: 'Perfil' },
    { href: '/praias', icon: Waves, label: 'Praias' },
    { href: '/usuarios', icon: Users, label: 'Usuários' },
    { href: '/mensagens', icon: MessageSquare, label: 'Mensagens' },
];

const SidebarLeft: React.FC = () => {
    const location = useLocation();
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.location.href = '/';
    };

    return (
        <aside className="w-60 h-[90%] flex flex-col justify-between p-4 bg-white border-r border-gray-200">
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

            <div>
                <Dialog open={isSettingsOpen} onOpenChange={setIsSettingsOpen}>
                    <DialogTrigger asChild>
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
                                <Button
                                    variant="ghost"
                                    className="w-full justify-start p-4 text-md text-red-600 hover:text-red-700 hover:bg-red-50 hover:cursor-pointer"
                                    onClick={handleLogout}
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
