import React, { useRef } from 'react';
import { Bell, Check, CheckCheck, X, Loader2 } from 'lucide-react';
import { useNotifications, getNotificationIcon } from '@/contexts/NotificationContext';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { useNavigate } from 'react-router-dom';
import type { NotificationDTO } from '@/api/services/notificationService';

interface NotificationDropdownProps {
  className?: string;
}

const NotificationDropdown: React.FC<NotificationDropdownProps> = ({ className = '' }) => {
  const {
    notifications,
    unreadCount,
    isLoading,
    showDropdown,
    setShowDropdown,
    markAsRead,
    markAllAsRead,
  } = useNotifications();

  const dropdownRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();



  const getNotificationColor = (type: string) => {
    switch (type) {
      case 'LIKE':
        return 'bg-red-50 border-red-100';
      case 'COMMENT':
        return 'bg-blue-50 border-blue-100';
      case 'REPLY':
        return 'bg-purple-50 border-purple-100';
      case 'MENTION':
        return 'bg-green-50 border-green-100';
      case 'FOLLOW':
        return 'bg-cyan-50 border-cyan-100';
      default:
        return 'bg-gray-50 border-gray-100';
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

    if (diffSecs < 60) return 'Agora';
    if (diffMins < 60) return `${diffMins}min`;
    if (diffHours < 24) return `${diffHours}h`;
    if (diffDays < 7) return `${diffDays}d`;
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
  };

  const handleNotificationClick = async (notif: NotificationDTO) => {
    if (!notif.read) {
      await markAsRead(notif.id);
    }

    setShowDropdown(false);

    if (notif.postId) {
      navigate(`/posts/${notif.postId}/comments`);
    } else if (notif.sender?.username) {
      navigate(`/perfil/${notif.sender.id}`);
    }
  };

  return (
    <div className={`relative ${className}`}>
      {/* Botão de notificações */}
      <button
        onClick={() => setShowDropdown(!showDropdown)}
        className="notification-button relative p-2 text-white hover:text-yellow-300 transition-all duration-200 hover:scale-110 active:scale-95"
        aria-label="Notificações"
      >
        <Bell size={26} className={unreadCount > 0 ? 'animate-pulse' : ''} />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs min-w-5 h-5 px-1 rounded-full flex items-center justify-center font-medium shadow-lg animate-in zoom-in-50 duration-200">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown de notificações */}
      {showDropdown && (
        <div
          ref={dropdownRef}
          className="notification-dropdown absolute right-0 mt-3 w-[calc(100vw-2rem)] sm:w-96 max-w-[400px] bg-white shadow-2xl rounded-2xl overflow-hidden z-[99999] border border-gray-100"
          style={{
            transformOrigin: 'top right',
            maxHeight: 'calc(100vh - 120px)'
          }}
        >
          {/* Header */}
          <div className="sticky top-0 px-4 py-3 border-b bg-[var(--primary)] flex items-center justify-between z-10">
            <div className="flex items-center gap-2">
              <Bell className="w-5 h-5 text-white" />
              <h3 className="font-semibold text-white text-lg">Notificações</h3>
              {unreadCount > 0 && (
                <span className="bg-blue-100 text-blue-600 text-xs px-2 py-0.5 rounded-full">
                  {unreadCount} nova{unreadCount > 1 ? 's' : ''}
                </span>
              )}
            </div>
            <div className="flex items-center gap-2">
              {unreadCount > 0 && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    markAllAsRead();
                  }}
                  className="text-white hover:text-yellow-300 p-1.5 rounded-lg hover:bg-white/10 transition-colors"
                  title="Marcar todas como lidas"
                >
                  <CheckCheck size={18} />
                </button>
              )}
              <button
                onClick={() => setShowDropdown(false)}
                className="text-white hover:text-yellow-300 p-1.5 rounded-lg hover:bg-white/10 transition-colors sm:hidden"
              >
                <X size={18} />
              </button>
            </div>
          </div>

          {/* Lista de notificações */}
          <div className="max-h-[60vh] overflow-y-auto overscroll-contain">
            {isLoading && notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-gray-400">
                <Loader2 className="w-8 h-8 animate-spin mb-2" />
                <p className="text-sm">Carregando...</p>
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 px-4 text-gray-400">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-3">
                  <Bell className="w-8 h-8 text-gray-300" />
                </div>
                <p className="text-sm font-medium text-gray-500">Nenhuma notificação</p>
                <p className="text-xs text-gray-400 text-center mt-1">
                  Você receberá notificações sobre curtidas, comentários e menções aqui
                </p>
              </div>
            ) : (
              <ul className="divide-y divide-gray-100">
                {notifications.map((notif, index) => (
                  <li
                    key={notif.id}
                    onClick={() => handleNotificationClick(notif)}
                    className={`relative px-4 py-3 cursor-pointer transition-all duration-200 hover:bg-gray-50 active:bg-gray-100 bg-white`}
                    style={{
                      animationDelay: `${index * 50}ms`,
                    }}
                  >
                    <div className="flex gap-3">
                      {/* Avatar do remetente */}
                      <div className="relative flex-shrink-0">
                        <Avatar className="w-10 h-10 ring-2 ring-white shadow-sm">
                          {notif.sender?.fotoPerfil ? (
                            <AvatarImage
                              src={notif.sender.fotoPerfil}
                              alt={notif.sender.username}
                            />
                          ) : (
                            <AvatarFallback className="bg-gradient-to-br from-[var(--primary)] to-blue-400 text-white text-sm font-medium">
                              {notif.sender?.username?.charAt(0).toUpperCase() || '?'}
                            </AvatarFallback>
                          )}
                        </Avatar>
                        {/* Ícone do tipo de notificação */}
                        <div
                          className={`absolute -bottom-1 -right-1 w-5 h-5 rounded-full flex items-center justify-center border-2 border-white shadow-sm ${getNotificationColor(notif.type)}`}
                        >
                          {getNotificationIcon(notif.type)}
                        </div>
                      </div>

                      {/* Conteúdo */}
                      <div className="flex-1 min-w-0">
                        <p className={`text-sm ${!notif.read ? 'font-medium text-gray-900' : 'text-gray-700'}`}>
                          {notif.message}
                        </p>
                        <p className="text-xs text-gray-400 mt-1 flex items-center gap-1">
                          {formatTimeAgo(notif.createdAt)}
                          {!notif.read && (
                            <span className="inline-block w-1.5 h-1.5 bg-blue-500 rounded-full ml-1" />
                          )}
                        </p>
                      </div>

                      {/* Botão de marcar como lida igual ao de remover */}
                      {!notif.read && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            markAsRead(notif.id);
                          }}
                          className="flex-shrink-0 p-2 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                          title="Marcar como lida"
                        >
                          <Check size={18} />
                        </button>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>


        </div>
      )}
    </div>
  );
};

export default NotificationDropdown;
