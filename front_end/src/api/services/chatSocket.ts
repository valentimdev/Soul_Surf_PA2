// src/api/services/chatSocket.ts
import { Client } from '@stomp/stompjs';

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

  // USE ws:// E WebSocket NATIVO
  const socket = new WebSocket(`ws://localhost:8080/ws?access_token=${token}`);

  const client = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: (str) => console.log('[STOMP]', str),

    onConnect: () => {
      console.log('STOMP CONECTADO! Assinando:', conversationId);
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