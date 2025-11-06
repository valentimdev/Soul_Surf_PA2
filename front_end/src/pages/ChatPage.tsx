// src/pages/ChatPage.tsx
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import SockJS from "sockjs-client";
import { Client, IMessage } from "@stomp/stompjs";

type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachmentUrl?: string | null;
  createdAt: string;
  editedAt?: string | null;
};

export default function ChatPage() {
  const { conversationId = "" } = useParams();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState("");
  const clientRef = useRef<Client | null>(null);
  const subRef = useRef<string | null>(null);
  const endRef = useRef<HTMLDivElement | null>(null);

  const token = localStorage.getItem("token") ?? "";
  const userEmail = localStorage.getItem("email") ?? "";

  // auto-scroll
  const scrollToBottom = () => endRef.current?.scrollIntoView({ behavior: "smooth" });

  // carrega histórico
  async function loadMessages() {
    const r = await axios.get(
      `/api/chat/conversations/${conversationId}/messages?page=0&size=50`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    // backend pode devolver Page ou array puro
    const data = Array.isArray(r.data) ? r.data : (r.data.content ?? []);
    // se vier DESC, inverte para exibir ascendente
    setMessages([...data].reverse());
  }

  // conecta e assina a conversa
  function connectAndSubscribe() {
    // encerra conexão anterior (se houver)
    if (clientRef.current) {
      try { clientRef.current.deactivate(); } catch {}
      clientRef.current = null;
      subRef.current = null;
    }

    const sock = new SockJS("/ws"); // use PROXY do Vite para 8080
    const client = new Client({
      webSocketFactory: () => sock as any,
      connectHeaders: { Authorization: `Bearer ${token}` }, // JwtChatChannelInterceptor
      reconnectDelay: 5000,
      onConnect: () => {
        // assina o tópico da conversa
        const sub = client.subscribe(`/topic/conversations/${conversationId}`, (frame: IMessage) => {
          const payload: ChatMessage = JSON.parse(frame.body);
          // evita duplicar se a UI otimista já adicionou (verificamos id)
          setMessages(prev =>
            prev.some(m => m.id === payload.id) ? prev : [...prev, payload]
          );
        });
        subRef.current = sub.id;
      },
      onStompError: f => console.error("STOMP error:", f.headers["message"]),
      debug: () => {} // silencioso
    });

    client.activate();
    clientRef.current = client;
  }

  // envia mensagem (UI otimista)
  async function sendMessage() {
    const body = content.trim();
    if (!body) return;

    // opcional: UI otimista
    const temp: ChatMessage = {
      id: `tmp-${Date.now()}`,
      conversationId,
      senderId: userEmail,
      content: body,
      attachmentUrl: null,
      createdAt: new Date().toISOString(),
      editedAt: null,
    };
    setMessages(prev => [...prev, temp]);
    setContent("");

    try {
      const r = await axios.post(
        `/api/chat/conversations/${conversationId}/messages`,
        { content: body },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const saved: ChatMessage = r.data;
      // substitui a temp pela salva (mesmo conteúdo, id real)
      setMessages(prev =>
        prev.map(m => (m.id === temp.id ? saved : m))
      );
      // o push do servidor também chegará; o de-dup acima evita repetição
    } catch (err) {
      // rollback da UI otimista
      setMessages(prev => prev.filter(m => m.id !== temp.id));
      alert("Não foi possível enviar a mensagem.");
      console.error(err);
    }
  }

  // efeitos
  useEffect(() => {
    if (!conversationId) return;
    loadMessages();
    connectAndSubscribe();
    return () => {
      // cleanup ao desmontar/trocar de conversa
      try { clientRef.current?.deactivate(); } catch {}
      clientRef.current = null;
      subRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [conversationId]);

  useEffect(() => { scrollToBottom(); }, [messages]);

  return (
    <div style={styles.container}>
      <div style={styles.chatBox}>
        {messages.map(m => {
          const mine = m.senderId === userEmail;
          return (
            <div
              key={m.id}
              style={{
                ...styles.message,
                alignSelf: mine ? "flex-end" : "flex-start",
                background: mine ? "#4f46e5" : "#e5e7eb",
                color: mine ? "#fff" : "#111827",
              }}
              title={new Date(m.createdAt).toLocaleString()}
            >
              <b>{(m.senderId || "").split("@")[0]}</b>: {m.content}
            </div>
          );
        })}
        <div ref={endRef} />
      </div>

      <div style={styles.inputArea}>
        <input
          value={content}
          onChange={e => setContent(e.target.value)}
          onKeyDown={e => e.key === "Enter" && sendMessage()}
          placeholder="Digite sua mensagem…"
          style={styles.input}
        />
        <button onClick={sendMessage} style={styles.button}>Enviar</button>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: { display: "flex", flexDirection: "column", height: "90vh", padding: 20, background: "#f3f4f6" },
  chatBox: { flex: 1, overflowY: "auto", display: "flex", flexDirection: "column", gap: 10, padding: 10, background: "#fff", borderRadius: 10, boxShadow: "0 2px 8px rgba(0,0,0,0.1)" },
  message: { maxWidth: "70%", padding: "10px 14px", borderRadius: 12, fontSize: 15, lineHeight: 1.4 },
  inputArea: { marginTop: 15, display: "flex", gap: 10 },
  input: { flex: 1, padding: 10, borderRadius: 8, border: "1px solid #ccc", fontSize: 15 },
  button: { background: "#4f46e5", color: "#fff", padding: "10px 18px", border: "none", borderRadius: 8, cursor: "pointer" },
};
