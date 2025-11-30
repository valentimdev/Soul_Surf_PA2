// src/api/services/chatSocket.ts
import { Client } from '@stomp/stompjs';

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

  const isLocalhost = window.location.hostname === 'localhost';

  const baseWsUrl = isLocalhost
    ? 'ws://localhost:8080/ws'
    : 'wss://soulsurfpa2-production.up.railway.app/ws';

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: (str) => console.log('[STOMP]', str),

    onConnect: () => {
      console.log('STOMP CONECTADO! Assinando tópico:', `/topic/conversations/${conversationId}`);

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
      console.error('Erro WebSocket:', error);
      onError?.(error);
    },

    onWebSocketClose: (event) => {
      console.log('WebSocket fechado:', event.code, event.reason);
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

  const isLocalhost = window.location.hostname === 'localhost';

  const baseWsUrl = isLocalhost
    ? 'ws://localhost:8080/ws'
    : 'wss://soulsurfpa2-production.up.railway.app/ws';

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: (str) => console.log('[STOMP][POST]', str),

    onConnect: () => {
      console.log('STOMP CONECTADO! Assinando tópicos de post:', postId);

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
      console.error('[POST] Erro WebSocket:', error);
      handlers.onError?.(error);
    },

    onWebSocketClose: (event) => {
      console.log('[POST] WebSocket fechado:', event.code, event.reason);
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

  const isLocalhost = window.location.hostname === 'localhost';

  const baseWsUrl = isLocalhost
    ? 'ws://localhost:8080/ws'
    : 'wss://soulsurfpa2-production.up.railway.app/ws';

  const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

  const client = new Client({
    webSocketFactory: () => new WebSocket(socketUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: (str) => console.log('[STOMP][NOTIFICATIONS]', str),

    onConnect: () => {
      console.log('STOMP CONECTADO! Assinando notificações do usuário:', username);

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
      console.error('[NOTIFICATIONS] Erro WebSocket:', error);
      handlers.onError?.(error);
    },

    onWebSocketClose: (event) => {
      console.log('[NOTIFICATIONS] WebSocket fechado:', event.code, event.reason);
    },
  });

  client.activate();
  return client;
};
