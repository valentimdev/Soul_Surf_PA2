// src/pages/ChatPage.tsx
import { useEffect, useRef, useState, useMemo } from "react";
import { useParams } from "react-router-dom";
import { api } from "@/api/axios"; // ⬅️ usa seu axios com baseURL
import { connectChat } from "@/api/services/chatSocket";
import { format } from "date-fns";
import { Send, Paperclip, Smile } from "lucide-react";
import { UserService } from "@/api/services/userService";

type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachmentUrl?: string | null;
  createdAt: string;
  editedAt?: string | null;
  pending?: boolean; // indica se ainda não foi enviado
};

export default function ChatPage() {
  const { conversationId = "" } = useParams();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [me, setMe] = useState<{ email: string } | null>(null);

  const clientRef = useRef<any>(null);
  const endRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const token = localStorage.getItem("token") ?? "";

  useEffect(() => {
    UserService.getMe().then((user) => setMe({ email: user.email }));
  }, []);

  const allMessages = useMemo(() => {
    const map = new Map<string, ChatMessage>();
    messages.forEach((msg) => map.set(msg.id, msg));
    return Array.from(map.values()).sort(
      (a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
  }, [messages]);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth", block: "nearest" });
  }, [allMessages]);

  const loadMessages = async () => {
    try {
      const r = await api.get(
        `/api/chat/conversations/${conversationId}/messages?page=0&size=50`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const data = Array.isArray(r.data) ? r.data : r.data.content ?? [];
      setMessages(data.reverse());
    } catch (err) {
      console.error("Erro ao carregar mensagens:", err);
    }
  };

  const connectAndSubscribe = () => {
    if (clientRef.current) clientRef.current.deactivate();
    if (!token || !conversationId) return;

    const client = connectChat(token, conversationId, (payload: ChatMessage) => {
      setMessages((prev) =>
        prev.some((m) => m.id === payload.id) ? prev : [...prev, payload]
      );
    });

    clientRef.current = client;
  };

  const sendMessage = async () => {
    if (!me) return;
    const body = content.trim();
    if (!body) return;

    const tempId = `tmp-${Date.now()}`;
    const tempMessage: ChatMessage = {
      id: tempId,
      conversationId,
      senderId: me.email,
      content: body,
      createdAt: new Date().toISOString(),
      pending: true,
    };

    setMessages((prev) => [...prev, tempMessage]);
    setContent("");
    setIsTyping(false);

    try {
      const r = await api.post(
        `/api/chat/conversations/${conversationId}/messages`,
        { content: body },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const saved: ChatMessage = r.data;
      setMessages((prev) => prev.map((m) => (m.id === tempId ? saved : m)));
    } catch (err: any) {
      console.error("Erro ao enviar:", err.response?.data || err);
      setMessages((prev) => prev.filter((m) => m.id !== tempId));
      alert("Falha ao enviar. Tente novamente.");
    }
  };

  useEffect(() => {
    if (!conversationId || !token) return;
    loadMessages();
    connectAndSubscribe();
    return () => clientRef.current?.deactivate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [conversationId, token]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      {/* Header fixo */}
      <header className="sticky top-0 z-10 bg-white border-b border-slate-200 px-6 py-4 shadow-sm">
        <div className="max-w-4xl mx-auto flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm">
            {me?.email ? me.email[0].toUpperCase() : "?"}
          </div>
          <div>
            <h1 className="text-lg font-semibold text-slate-800">
              Chat em Tempo Real
            </h1>
            <p className="text-xs text-slate-500">Conversa ativa</p>
          </div>
        </div>
      </header>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-4 py-6 max-w-4xl w-full mx-auto">
        <div className="space-y-0">
          {allMessages.map((m, idx) => {
            const isMine = me?.email === m.senderId;
            const prev = allMessages[idx - 1];
            const marginTop =
              prev && prev.senderId === m.senderId ? "mt-1" : "mt-4";

            return (
              <div
                key={m.id}
                className={`flex ${
                  isMine ? "justify-start" : "justify-end"
                } items-end ${marginTop}`}
              >
                <div
                  className={`max-w-xs md:max-w-md px-4 py-3 rounded-2xl shadow-sm break-words relative
                    ${
                      isMine
                        ? "bg-gray-200 text-slate-800 border border-slate-300 mr-auto"
                        : "bg-gradient-to-br from-indigo-500 to-purple-600 text-white ml-auto"
                    }`}
                >
                  {!isMine && (
                    <p className="text-xs font-medium text-indigo-100 mb-1">
                      {m.senderId.split("@")[0]}
                    </p>
                  )}
                  <p className="text-sm leading-relaxed">{m.content}</p>
                  <p
                    className={`text-xs mt-1 ${
                      isMine ? "text-slate-400" : "text-indigo-100"
                    }`}
                  >
                    {format(new Date(m.createdAt), "HH:mm")}
                  </p>

                  {m.pending && (
                    <div className="absolute bottom-1 right-1 w-3 h-3 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                  )}
                </div>
              </div>
            );
          })}

          {isTyping && (
            <div className="flex justify-start mt-1">
              <div className="bg-white px-4 py-3 rounded-2xl shadow-sm border border-slate-200">
                <div className="flex gap-1">
                  <div
                    className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"
                    style={{ animationDelay: "0ms" }}
                  />
                  <div
                    className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"
                    style={{ animationDelay: "150ms" }}
                  />
                  <div
                    className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"
                    style={{ animationDelay: "300ms" }}
                  />
                </div>
              </div>
            </div>
          )}

          <div ref={endRef} />
        </div>
      </div>

      {/* Input fixo */}
      <div className="sticky bottom-0 z-10 border-t border-slate-200 bg-white px-4 py-4">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-end gap-2">
            <button className="p-2 text-slate-500 hover:text-indigo-600 transition-colors">
              <Paperclip className="w-5 h-5" />
            </button>

            <div className="flex-1 relative">
              <input
                ref={inputRef}
                value={content}
                onChange={(e) => {
                  setContent(e.target.value);
                  setIsTyping(e.target.value.length > 0);
                }}
                onKeyDown={handleKeyDown}
                placeholder="Digite sua mensagem..."
                className="w-full px-4 py-3 pr-12 bg-slate-100 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition-all"
              />
              <button className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-indigo-600">
                <Smile className="w-5 h-5" />
              </button>
            </div>

            <button
              onClick={sendMessage}
              disabled={!content.trim()}
              className={`p-3 rounded-xl transition-all duration-200 flex items-center gap-2
                ${
                  content.trim()
                    ? "bg-gradient-to-br from-indigo-500 to-purple-600 text-white shadow-lg hover:shadow-xl transform hover:scale-105"
                    : "bg-slate-200 text-slate-400 cursor-not-allowed"
                }`}
            >
              <Send className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
