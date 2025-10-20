// src/services/chatSocket.ts
import { Client, type IMessage } from "@stomp/stompjs";

const WS_URL = (import.meta.env.VITE_WS_URL as string) || "ws://localhost:8080/ws";
// NOTE: com WebSocket nativo use ws:// (ou wss:// em produção)

export function connectChat(
  token: string,
  conversationId: string,
  onMessage: (parsedBody: any, raw: IMessage) => void
) {
  const client = new Client({
    brokerURL: WS_URL, // nativo, sem webSocketFactory/SockJS
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/conversations/${conversationId}`, (msg) => {
        const payload = JSON.parse(msg.body);
        onMessage(payload, msg);
      });
    },
    onStompError: (f) => console.error("STOMP error:", f.headers["message"]),
  });

  client.activate();
  return client;
}
