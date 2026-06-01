// src/api/services/chatSocket.ts
import { Client } from '@stomp/stompjs';

function resolveWsBaseUrl(): string {
  const configuredUrl = (import.meta as any).env?.VITE_WS_URL?.replace(/\/+$/, "");
  if (configuredUrl) return configuredUrl;

  return window.location.hostname === 'localhost'
    ? 'ws://localhost:8080/ws'
    : 'wss://soulsurfpa2-production.up.railway.app/ws';
}

/**
 * Conexão para CHAT (já existia)
 */
export const connectChat = (
  token: string,
  conversationId: string,
  onMessageReceived: (msg: any) => void,
  onError?: (error: any) => void
) => {
  if (!token) {
    console.error('Token JWT não encontrado');
    return null;
  }

  const baseWsUrl = resolveWsBaseUrl();

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  let reconnectAttempts = 0;
  const maxReconnectAttempts = 3;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: () => {}, // Desabilitar logs de debug

    onConnect: () => {
      reconnectAttempts = 0; // Reset contador ao conectar
      client.subscribe(`/topic/conversations/${conversationId}`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          onMessageReceived(payload);
        } catch (e) {
          console.error('Erro ao parsear mensagem:', e);
        }
      });
    },

    onStompError: (frame) => {
      console.error('Erro STOMP:', frame.headers['message']);
      onError?.(frame);
    },

    onWebSocketError: (error) => {
      reconnectAttempts++;
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('Muitas tentativas de reconexão. Desabilitando WebSocket.');
        client.deactivate();
        onError?.(error);
        return;
      }
      onError?.(error);
    },

    onWebSocketClose: (_event) => {
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('WebSocket desabilitado após muitas tentativas.');
      }
    },
  });

  client.activate();
  return client;
};

/**
 * 🔥 NOVO: conexão em tempo real para LIKES + COMENTÁRIOS de um post
 */
export const connectPostRealtime = (
  token: string,
  postId: string | number,
  handlers: {
    onLikeUpdate?: (event: {
      postId: number;
      likesCount: number;
      username: string;
      liked: boolean;
    }) => void;
    onCommentEvent?: (event: {
      type: 'CREATED' | 'UPDATED' | 'DELETED';
      postId: number;
      comment: any; // CommentDTO do backend
    }) => void;
    onError?: (error: any) => void;
  }
) => {
  if (!token) {
    console.error('Token JWT não encontrado');
    return null;
  }

  const baseWsUrl = resolveWsBaseUrl();

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  let reconnectAttempts = 0;
  const maxReconnectAttempts = 3;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: () => {}, // Desabilitar logs de debug

    onConnect: () => {
      reconnectAttempts = 0; // Reset contador ao conectar
      // 👍 Likes em tempo real
      client.subscribe(`/topic/posts/${postId}/likes`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          // payload = { postId, likesCount, username, liked }
          handlers.onLikeUpdate?.(payload);
        } catch (e) {
          console.error('Erro ao parsear mensagem de like:', e);
        }
      });

      // 💬 Comentários em tempo real
      client.subscribe(`/topic/posts/${postId}/comments`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          // payload = { type: 'CREATED' | 'UPDATED' | 'DELETED', postId, comment }
          handlers.onCommentEvent?.(payload);
        } catch (e) {
          console.error('Erro ao parsear mensagem de comentário:', e);
        }
      });
    },

    onStompError: (frame) => {
      console.error('[POST] Erro STOMP:', frame.headers['message']);
      handlers.onError?.(frame);
    },

    onWebSocketError: (error) => {
      reconnectAttempts++;
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('[POST] Muitas tentativas de reconexão. Desabilitando WebSocket.');
        client.deactivate();
        handlers.onError?.(error);
        return;
      }
      handlers.onError?.(error);
    },

    onWebSocketClose: (_event) => {
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('[POST] WebSocket desabilitado após muitas tentativas.');
      }
    },
  });

  client.activate();
  return client;
};

/**
 * 🔔 NOVO: conexão em tempo real para NOTIFICAÇÕES do usuário
 */
export const connectNotifications = (
  token: string,
  username: string,
  handlers: {
    onNotification?: (notification: {
      id: number;
      sender: any;
      type: string;
      postId: number | null;
      commentId: number | null;
      read: boolean;
      createdAt: string;
      message: string;
    }) => void;
    onError?: (error: any) => void;
  }
) => {
  if (!token) {
    console.error('Token JWT não encontrado');
    return null;
  }

  const baseWsUrl = resolveWsBaseUrl();

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  let reconnectAttempts = 0;
  const maxReconnectAttempts = 3;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: () => {}, // Desabilitar logs de debug

    onConnect: () => {
      reconnectAttempts = 0; // Reset contador ao conectar
      // 🔔 Notificações em tempo real
      client.subscribe(`/topic/notifications/${username}`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          handlers.onNotification?.(payload);
        } catch (e) {
          console.error('Erro ao parsear notificação:', e);
        }
      });
    },

    onStompError: (frame) => {
      console.error('[NOTIFICATIONS] Erro STOMP:', frame.headers['message']);
      handlers.onError?.(frame);
    },

    onWebSocketError: (error) => {
      reconnectAttempts++;
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('[NOTIFICATIONS] Muitas tentativas de reconexão. Desabilitando WebSocket.');
        client.deactivate();
        handlers.onError?.(error);
        return;
      }
      handlers.onError?.(error);
    },

    onWebSocketClose: (_event) => {
      if (reconnectAttempts >= maxReconnectAttempts) {
        console.warn('[NOTIFICATIONS] WebSocket desabilitado após muitas tentativas.');
      }
    },
  });

  client.activate();
  return client;
};
