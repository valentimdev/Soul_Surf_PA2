import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { NotificationService, type NotificationDTO } from '@/api/services/notificationService';
import { connectNotifications } from '@/api/services/chatSocket';
import { UserService } from '@/api/services/userService';
import { useAuth } from './AuthContext';
import { toast } from 'sonner';
import { Heart, MessageCircle, AtSign, UserPlus, Bell, CornerDownRight } from 'lucide-react';
import type { Client } from '@stomp/stompjs';

interface NotificationContextType {
  notifications: NotificationDTO[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
  isConnected: boolean;
  fetchNotifications: () => Promise<void>;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  clearNotification: (id: number) => void;
  clearAllNotifications: () => void;
  showDropdown: boolean;
  setShowDropdown: (show: boolean) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: React.ReactNode;
}

// Helper para obter ícone React do tipo de notificação
export const getNotificationIcon = (type: string, size: 'sm' | 'md' | 'lg' = 'sm') => {
  const sizeClass = size === 'sm' ? 'w-4 h-4' : size === 'md' ? 'w-5 h-5' : 'w-6 h-6';
  switch (type) {
    case 'LIKE':
      return <Heart className={`${sizeClass} text-red-500`} fill="currentColor" />;
    case 'COMMENT':
      return <MessageCircle className={`${sizeClass} text-blue-500`} />;
    case 'REPLY':
      return <CornerDownRight className={`${sizeClass} text-purple-500`} />;
    case 'MENTION':
      return <AtSign className={`${sizeClass} text-green-500`} />;
    case 'FOLLOW':
      return <UserPlus className={`${sizeClass} text-cyan-500`} />;
    default:
      return <Bell className={`${sizeClass} text-gray-500`} />;
  }
};

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const [username, setUsername] = useState<string | null>(null);
  const { token } = useAuth();
  const wsClientRef = useRef<Client | null>(null);
  const isFirstLoad = useRef(true);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const fetchNotifications = useCallback(async () => {
    if (!token) return;

    try {
      setIsLoading(true);
      setError(null);
      const notifs = await NotificationService.getMyNotifications();

      // Ordenar por data (mais recentes primeiro)
      const sortedNotifs = notifs.sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );

      setNotifications(sortedNotifs);
      isFirstLoad.current = false;
    } catch (err) {
      setError('Erro ao carregar notificações');
      console.error('Erro ao buscar notificações:', err);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  const showNotificationToast = useCallback((notif: NotificationDTO) => {
    toast(notif.message, {
      icon: getNotificationIcon(notif.type),
      duration: 5000,
      position: 'top-right',
      action: notif.postId
        ? {
            label: 'Ver',
            onClick: () => {
              window.location.href = `/posts/${notif.postId}/comments`;
            },
          }
        : undefined,
    });
  }, []);

  // Adicionar nova notificação em tempo real
  const addNotification = useCallback((notif: NotificationDTO) => {
    setNotifications((prev) => {
      // Evitar duplicatas
      if (prev.some((n) => n.id === notif.id)) {
        return prev;
      }
      // Adicionar no início (mais recente)
      return [notif, ...prev];
    });

    // Mostrar toast
    showNotificationToast(notif);
  }, [showNotificationToast]);

  const markAsRead = useCallback(async (id: number) => {
    try {
      await NotificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch (err) {
      console.error('Erro ao marcar notificação como lida:', err);
      toast.error('Erro ao marcar como lida');
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      const unreadNotifications = notifications.filter((n) => !n.read);
      await Promise.all(
        unreadNotifications.map((n) => NotificationService.markAsRead(n.id))
      );
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      toast.success('Todas marcadas como lidas');
    } catch (err) {
      console.error('Erro ao marcar todas como lidas:', err);
      toast.error('Erro ao marcar todas como lidas');
    }
  }, [notifications]);

  const clearNotification = useCallback((id: number) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const clearAllNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  // Buscar usuário atual para obter username
  useEffect(() => {
    if (!token) {
      setUsername(null);
      return;
    }

    const fetchUser = async () => {
      try {
        const user = await UserService.getMe();
        setUsername(user.username);
      } catch (error) {
        console.error('Erro ao buscar usuário:', error);
        setUsername(null);
      }
    };

    fetchUser();
  }, [token]);

  // Conexão WebSocket para notificações em tempo real
  useEffect(() => {
    if (!token || !username) {
      setNotifications([]);
      setIsConnected(false);
      // Limpar cliente se existir
      if (wsClientRef.current) {
        if (wsClientRef.current.connected) {
          wsClientRef.current.deactivate();
        }
        wsClientRef.current = null;
      }
      return;
    }

    // Buscar notificações iniciais
    fetchNotifications();

    // Conectar WebSocket
    const client = connectNotifications(token, username, {
      onNotification: (notification) => {
        addNotification(notification as NotificationDTO);
      },
      onError: (error) => {
        setIsConnected(false);
        // Não logar erros repetitivos de conexão
      },
    });

    if (client) {
      wsClientRef.current = client;

      // Verificar conexão após um tempo
      const checkConnection = setTimeout(() => {
        if (client.connected) {
          setIsConnected(true);
        }
      }, 2000);

      return () => {
        clearTimeout(checkConnection);
        if (client && client.connected) {
          client.deactivate();
        }
        wsClientRef.current = null;
        setIsConnected(false);
      };
    }

    // Fallback: polling se WebSocket não conectar
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [token, username, fetchNotifications, addNotification]);

  // Fechar dropdown ao clicar fora
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (
        !target.closest('.notification-dropdown') &&
        !target.closest('.notification-button')
      ) {
        setShowDropdown(false);
      }
    };

    if (showDropdown) {
      document.addEventListener('click', handleClickOutside);
    }

    return () => document.removeEventListener('click', handleClickOutside);
  }, [showDropdown]);

  const value: NotificationContextType = {
    notifications,
    unreadCount,
    isLoading,
    error,
    isConnected,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    clearNotification,
    clearAllNotifications,
    showDropdown,
    setShowDropdown,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};
