import React, { useState } from 'react';
import {
  Bell,
  Check,
  CheckCheck,
  Trash2,
  MessageCircle,
  Heart,
  AtSign,
  UserPlus,
  ArrowLeft,
  Loader2,
  Wifi,
} from 'lucide-react';
import { useNotifications, getNotificationIcon } from '@/contexts/NotificationContext';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';
import type { NotificationDTO } from '@/api/services/notificationService';

type FilterType = 'all' | 'unread' | 'LIKE' | 'COMMENT' | 'MENTION' | 'FOLLOW';

const NotificationsPage: React.FC = () => {
  const {
    notifications,
    unreadCount,
    isLoading,
    isConnected,
    markAsRead,
    markAllAsRead,
    clearNotification,
    clearAllNotifications,
  } = useNotifications();

  const [filter, setFilter] = useState<FilterType>('all');
  const navigate = useNavigate();

  const getNotificationBgColor = (type: string, isRead: boolean) => {
    if (isRead) return 'bg-white';
    switch (type) {
      case 'LIKE':
        return 'bg-red-50';
      case 'COMMENT':
        return 'bg-blue-50';
      case 'REPLY':
        return 'bg-purple-50';
      case 'MENTION':
        return 'bg-green-50';
      case 'FOLLOW':
        return 'bg-cyan-50';
      default:
        return 'bg-gray-50';
    }
  };

  const formatTimeAgo = (dateString: string) => {
    const now = new Date();
    const date = new Date(dateString);
    const diffMs = now.getTime() - date.getTime();
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    const diffWeeks = Math.floor(diffDays / 7);

    if (diffSecs < 60) return 'Agora mesmo';
    if (diffMins < 60) return `${diffMins} minuto${diffMins > 1 ? 's' : ''} atrás`;
    if (diffHours < 24) return `${diffHours} hora${diffHours > 1 ? 's' : ''} atrás`;
    if (diffDays < 7) return `${diffDays} dia${diffDays > 1 ? 's' : ''} atrás`;
    if (diffWeeks < 4) return `${diffWeeks} semana${diffWeeks > 1 ? 's' : ''} atrás`;
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
    });
  };

  const handleNotificationClick = async (notif: NotificationDTO) => {
    if (!notif.read) {
      await markAsRead(notif.id);
    }

    if (notif.postId) {
      navigate(`/posts/${notif.postId}/comments`);
    } else if (notif.sender?.username) {
      navigate(`/perfil/${notif.sender.id}`);
    }
  };

  const filteredNotifications = notifications.filter((notif) => {
    if (filter === 'all') return true;
    if (filter === 'unread') return !notif.read;
    return notif.type === filter;
  });

  const groupNotificationsByDate = (notifs: NotificationDTO[]) => {
    const groups: { [key: string]: NotificationDTO[] } = {};
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const thisWeek = new Date(today);
    thisWeek.setDate(thisWeek.getDate() - 7);

    notifs.forEach((notif) => {
      const date = new Date(notif.createdAt);
      date.setHours(0, 0, 0, 0);

      let groupKey: string;
      if (date.getTime() >= today.getTime()) {
        groupKey = 'Hoje';
      } else if (date.getTime() >= yesterday.getTime()) {
        groupKey = 'Ontem';
      } else if (date.getTime() >= thisWeek.getTime()) {
        groupKey = 'Esta semana';
      } else {
        groupKey = 'Anteriores';
      }

      if (!groups[groupKey]) {
        groups[groupKey] = [];
      }
      groups[groupKey].push(notif);
    });

    return groups;
  };

  const groupedNotifications = groupNotificationsByDate(filteredNotifications);
  const groupOrder = ['Hoje', 'Ontem', 'Esta semana', 'Anteriores'];

  const filterOptions: { value: FilterType; label: string; icon: React.ReactNode }[] = [
    { value: 'all', label: 'Todas', icon: <Bell size={16} /> },
    { value: 'unread', label: 'Não lidas', icon: <span className="w-2 h-2 bg-blue-500 rounded-full" /> },
    { value: 'LIKE', label: 'Curtidas', icon: <Heart size={16} className="text-red-500" /> },
    { value: 'COMMENT', label: 'Comentários', icon: <MessageCircle size={16} className="text-blue-500" /> },
    { value: 'MENTION', label: 'Menções', icon: <AtSign size={16} className="text-green-500" /> },
    { value: 'FOLLOW', label: 'Seguidores', icon: <UserPlus size={16} className="text-cyan-500" /> },
  ];

  return (
    <div className="min-h-screen bg-[var(--background)]">
      {/* Header */}
      <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-lg border-b border-gray-200 px-4 py-3">
        <div className="max-w-2xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate(-1)}
              className="p-2 -ml-2 rounded-full hover:bg-gray-100 transition-colors md:hidden"
            >
              <ArrowLeft size={20} />
            </button>
            <div>
              <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                <Bell className="w-6 h-6 text-[var(--primary)]" />
                Notificações
                {isConnected && (
                  <span className="flex items-center gap-1 text-xs font-normal text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                    <Wifi size={12} />
                    Tempo real
                  </span>
                )}
              </h1>
              {unreadCount > 0 && (
                <p className="text-sm text-gray-500">
                  {unreadCount} não lida{unreadCount > 1 ? 's' : ''}
                </p>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2">
            {unreadCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={markAllAsRead}
                className="text-[var(--primary)] hover:bg-[var(--primary)]/10"
              >
                <CheckCheck size={18} className="mr-1" />
                <span className="hidden sm:inline">Marcar todas</span>
              </Button>
            )}
          </div>
        </div>
      </header>

      {/* Filtros */}
      <div className="sticky top-[61px] z-20 bg-white/80 backdrop-blur-lg border-b border-gray-100 px-4 py-2 overflow-x-auto scrollbar-hide">
        <div className="max-w-2xl mx-auto flex gap-2">
          {filterOptions.map((option) => (
            <button
              key={option.value}
              onClick={() => setFilter(option.value)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                filter === option.value
                  ? 'bg-[var(--primary)] text-white shadow-sm'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {option.icon}
              {option.label}
              {option.value === 'unread' && unreadCount > 0 && (
                <span className={`ml-1 px-1.5 py-0.5 rounded-full text-xs ${
                  filter === option.value ? 'bg-white/20' : 'bg-blue-100 text-blue-600'
                }`}>
                  {unreadCount}
                </span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Conteúdo */}
      <main className="max-w-2xl mx-auto px-4 py-4">
        {isLoading && notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-gray-400">
            <Loader2 className="w-10 h-10 animate-spin mb-3" />
            <p className="text-sm">Carregando notificações...</p>
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 px-4">
            <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              {filter === 'all' ? (
                <Bell className="w-12 h-12 text-gray-300" />
              ) : filter === 'LIKE' ? (
                <Heart className="w-12 h-12 text-gray-300" />
              ) : filter === 'COMMENT' ? (
                <MessageCircle className="w-12 h-12 text-gray-300" />
              ) : filter === 'MENTION' ? (
                <AtSign className="w-12 h-12 text-gray-300" />
              ) : (
                <Bell className="w-12 h-12 text-gray-300" />
              )}
            </div>
            <h3 className="text-lg font-semibold text-gray-700 mb-1">
              {filter === 'all'
                ? 'Nenhuma notificação'
                : filter === 'unread'
                ? 'Tudo em dia!'
                : `Nenhuma ${filterOptions.find((f) => f.value === filter)?.label.toLowerCase()}`}
            </h3>
            <p className="text-sm text-gray-500 text-center max-w-xs">
              {filter === 'all'
                ? 'Quando você receber curtidas, comentários ou menções, elas aparecerão aqui'
                : filter === 'unread'
                ? 'Você não tem notificações não lidas'
                : `Suas ${filterOptions.find((f) => f.value === filter)?.label.toLowerCase()} aparecerão aqui`}
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {groupOrder.map((groupKey) => {
              const groupNotifs = groupedNotifications[groupKey];
              if (!groupNotifs || groupNotifs.length === 0) return null;

              return (
                <section key={groupKey}>
                  <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3 px-1">
                    {groupKey}
                  </h2>
                  <div className="space-y-2">
                    {groupNotifs.map((notif, index) => (
                      <div
                        key={notif.id}
                        onClick={() => handleNotificationClick(notif)}
                        className={`group relative rounded-xl p-4 cursor-pointer transition-all duration-200 hover:shadow-md active:scale-[0.99] border ${
                          getNotificationBgColor(notif.type, notif.read)
                        } ${!notif.read ? 'border-l-4 border-l-[var(--primary)]' : 'border-gray-100'}`}
                        style={{
                          animation: `slideInUp 0.3s ease-out ${index * 50}ms both`,
                        }}
                      >
                        <div className="flex gap-4">
                          {/* Avatar */}
                          <div className="relative flex-shrink-0">
                            <Avatar className="w-12 h-12 ring-2 ring-white shadow-md">
                              {notif.sender?.fotoPerfil ? (
                                <AvatarImage
                                  src={notif.sender.fotoPerfil}
                                  alt={notif.sender.username}
                                />
                              ) : (
                                <AvatarFallback className="bg-gradient-to-br from-[var(--primary)] to-blue-400 text-white text-lg font-semibold">
                                  {notif.sender?.username?.charAt(0).toUpperCase() || '?'}
                                </AvatarFallback>
                              )}
                            </Avatar>
                            {/* Ícone do tipo */}
                            <div className="absolute -bottom-1 -right-1 w-6 h-6 bg-white rounded-full flex items-center justify-center shadow-sm">
                              {getNotificationIcon(notif.type, 'md')}
                            </div>
                          </div>

                          {/* Conteúdo */}
                          <div className="flex-1 min-w-0">
                            <p className={`text-base ${!notif.read ? 'font-semibold text-gray-900' : 'text-gray-700'}`}>
                              {notif.message}
                            </p>
                            <p className="text-sm text-gray-400 mt-1 flex items-center gap-2">
                              {formatTimeAgo(notif.createdAt)}
                              {!notif.read && (
                                <span className="inline-flex items-center gap-1 text-xs font-medium text-[var(--primary)]">
                                  <span className="w-1.5 h-1.5 bg-[var(--primary)] rounded-full animate-pulse" />
                                  Nova
                                </span>
                              )}
                            </p>
                          </div>

                          {/* Ações */}
                          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                            {!notif.read && (
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  markAsRead(notif.id);
                                }}
                                className="p-2 text-gray-400 hover:text-green-500 hover:bg-green-50 rounded-lg transition-colors"
                                title="Marcar como lida"
                              >
                                <Check size={18} />
                              </button>
                            )}
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                clearNotification(notif.id);
                              }}
                              className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                              title="Remover"
                            >
                              <Trash2 size={18} />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </section>
              );
            })}

            {/* Botão limpar todas */}
            {notifications.length > 0 && (
              <div className="pt-6 pb-10">
                <Button
                  variant="outline"
                  onClick={clearAllNotifications}
                  className="w-full text-gray-500 hover:text-red-500 hover:border-red-200 hover:bg-red-50"
                >
                  <Trash2 size={18} className="mr-2" />
                  Limpar todas as notificações
                </Button>
              </div>
            )}
          </div>
        )}
      </main>

      {/* CSS para animação */}
      <style>{`
        @keyframes slideInUp {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
        
        .scrollbar-hide {
          -ms-overflow-style: none;
          scrollbar-width: none;
        }
      `}</style>
    </div>
  );
};

export default NotificationsPage;
