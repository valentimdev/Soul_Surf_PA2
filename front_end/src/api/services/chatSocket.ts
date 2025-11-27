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

  // Descobre URL base do WS
  const isLocalhost = window.location.hostname === 'localhost';

  const baseWsUrl = isLocalhost
    ? 'ws://localhost:8080/ws' // backend local
    : 'wss://soulsurfpa2-production.up.railway.app/ws'; // backend Railway em produção

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
