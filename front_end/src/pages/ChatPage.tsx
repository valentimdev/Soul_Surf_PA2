import { useEffect, useRef, useState, useMemo, useCallback } from "react";
import { useParams } from "react-router-dom";
import api from "@/api/axios";
import { connectChat } from "@/api/services/chatSocket";
import { format, isToday, isYesterday } from "date-fns";
import { Send, Loader2, MessageCircle } from "lucide-react";
import { UserService } from "@/api/services/userService";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card } from "@/components/ui/card";

type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachmentUrl?: string | null;
  createdAt: string;
  editedAt?: string | null;
  pending?: boolean;
};

type Participant = {
  email: string;
  username: string;
  fotoPerfil?: string | null;
};

export default function ChatPage() {
  const { conversationId = "" } = useParams();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState("");
  const [me, setMe] = useState<Participant | null>(null);
  const [otherParticipant, setOtherParticipant] = useState<Participant | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);

  const clientRef = useRef<any>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const initialScrollDoneRef = useRef(false);
  const token = localStorage.getItem("token") ?? "";

  // Normaliza email para comparação consistente
  const normalizeEmail = useCallback((email: string | null | undefined): string => {
    if (!email) return "";
    return email.trim().toLowerCase();
  }, []);

  // Carrega dados do usuário logado
  useEffect(() => {
    const loadUser = async () => {
      try {
        const user = await UserService.getMe();
        setMe({
          email: (user.email || "").trim(),
          username: user.username,
          fotoPerfil: user.fotoPerfil,
        });
      } catch (err) {
        console.error("[CHAT] Erro ao carregar usuário:", err);
      }
    };
    loadUser();
  }, []);

  // Carrega informações do outro participante
  useEffect(() => {
    if (!conversationId || !token || !me) return;

    const loadConversationInfo = async () => {
      try {
        const response = await api.get(`/chat/conversations`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const conversations = Array.isArray(response.data) ? response.data : [];
        const currentConv = conversations.find((c: any) => c.id === conversationId);

        if (currentConv) {
          setOtherParticipant({
            email: (currentConv.otherUserId || "").trim(),
            username: currentConv.otherUserName || "Usuário",
            fotoPerfil: currentConv.otherUserAvatarUrl,
          });
        } else if (messages.length > 0) {
          // Fallback: identificar pelo senderId das mensagens
          const otherMessage = messages.find((m) => {
            return normalizeEmail(m.senderId) !== normalizeEmail(me.email);
          });
          if (otherMessage) {
            setOtherParticipant({
              email: otherMessage.senderId.trim(),
              username: "Usuário",
              fotoPerfil: null,
            });
          }
        }
      } catch (err) {
        console.error("Erro ao carregar informações da conversa:", err);
      }
    };

    loadConversationInfo();
  }, [conversationId, token, me, messages, normalizeEmail]);

  // Organiza e ordena mensagens (remove duplicatas e ordena por data)
  const allMessages = useMemo(() => {
    const messageMap = new Map<string, ChatMessage>();

    messages.forEach((msg) => {
      const normalized = {
        ...msg,
        senderId: (msg.senderId || "").trim(),
      };
      messageMap.set(normalized.id, normalized);
    });

    return Array.from(messageMap.values()).sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
  }, [messages]);

  // Scroll para o final
  const scrollToBottom = useCallback((smooth = false) => {
    if (messagesContainerRef.current) {
      const container = messagesContainerRef.current;
      container.scrollTop = container.scrollHeight;
    } else if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({
        behavior: smooth ? "smooth" : "auto",
        block: "end",
      });
    }
  }, []);

  // Scroll automático ao carregar mensagens pela primeira vez
  useEffect(() => {
    if (allMessages.length > 0 && !initialScrollDoneRef.current && !isLoading) {
      // Aguarda o DOM ser completamente renderizado
      const timer = setTimeout(() => {
        scrollToBottom(false);
        initialScrollDoneRef.current = true;
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [allMessages.length, isLoading, scrollToBottom]);

  // Scroll suave ao receber novas mensagens
  useEffect(() => {
    if (initialScrollDoneRef.current && allMessages.length > 0) {
      const timer = setTimeout(() => scrollToBottom(true), 150);
      return () => clearTimeout(timer);
    }
  }, [allMessages, scrollToBottom]);

  // Carrega mensagens do backend
  const loadMessages = useCallback(async () => {
    if (!conversationId || !token) return;

    setIsLoading(true);
    try {
      const response = await api.get(
        `/chat/conversations/${conversationId}/messages?page=0&size=100`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const data = Array.isArray(response.data) ? response.data : response.data.content ?? [];
      const normalizedMessages = data.map((msg: ChatMessage) => ({
        ...msg,
        senderId: (msg.senderId || "").trim(),
      }));

      setMessages(normalizedMessages.reverse());
      initialScrollDoneRef.current = false;
    } catch (err) {
      console.error("Erro ao carregar mensagens:", err);
    } finally {
      setIsLoading(false);
    }
  }, [conversationId, token]);

  // Conecta WebSocket
  const connectAndSubscribe = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.deactivate();
    }
    if (!token || !conversationId) return;

    const client = connectChat(token, conversationId, (payload: ChatMessage) => {
      const normalizedPayload: ChatMessage = {
        ...payload,
        senderId: (payload.senderId || "").trim(),
      };

      setMessages((prev) => {
        // Evita duplicatas
        if (prev.some((m) => m.id === normalizedPayload.id)) {
          return prev;
        }
        return [...prev, normalizedPayload];
      });
    });

    clientRef.current = client;
  }, [token, conversationId]);

  // Envia mensagem
  const sendMessage = useCallback(async () => {
    if (!me || !content.trim() || isSending) return;

    const body = content.trim();
    const tempId = `tmp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const myEmailNormalized = me.email.trim();

    // Mensagem otimista
    const tempMessage: ChatMessage = {
      id: tempId,
      conversationId,
      senderId: myEmailNormalized,
      content: body,
      createdAt: new Date().toISOString(),
      pending: true,
    };

    setMessages((prev) => [...prev, tempMessage]);
    setContent("");
    setIsSending(true);

    try {
      const response = await api.post(
        `/chat/conversations/${conversationId}/messages`,
        { content: body },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const saved: ChatMessage = response.data;
      const normalizedSaved: ChatMessage = {
        ...saved,
        senderId: (saved.senderId || myEmailNormalized).trim(),
      };

      setMessages((prev) => prev.map((m) => (m.id === tempId ? normalizedSaved : m)));
    } catch (err: any) {
      console.error("Erro ao enviar:", err.response?.data || err);
      setMessages((prev) => prev.filter((m) => m.id !== tempId));
      setContent(body);
    } finally {
      setIsSending(false);
    }
  }, [me, content, conversationId, token, isSending]);

  // Inicializa chat
  useEffect(() => {
    if (!conversationId || !token) return;

    loadMessages();
    connectAndSubscribe();

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
      initialScrollDoneRef.current = false;
    };
  }, [conversationId, token, loadMessages, connectAndSubscribe]);

  // Handler para Enter
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Formata data da mensagem
  const formatMessageDate = (date: string): string => {
    const messageDate = new Date(date);
    if (isToday(messageDate)) {
      return format(messageDate, "HH:mm");
    } else if (isYesterday(messageDate)) {
      return `Ontem às ${format(messageDate, "HH:mm")}`;
    } else {
      return format(messageDate, "dd/MM/yyyy 'às' HH:mm");
    }
  };

  // Verifica se a mensagem é minha
  const isMyMessage = useCallback((messageSenderId: string): boolean => {
    if (!me?.email) return false;
    return normalizeEmail(messageSenderId) === normalizeEmail(me.email);
  }, [me, normalizeEmail]);

  const displayName = otherParticipant?.username || "Usuário";

  // Loading inicial
  if (isLoading && messages.length === 0) {
    return (
      <div className="w-full max-w-4xl mx-auto p-4 flex items-center justify-center min-h-[400px]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
          <p className="text-muted-foreground">Carregando conversa...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-4xl mx-auto p-3 sm:p-4 md:p-6 flex flex-col h-[calc(100vh-140px)] min-h-[500px]">
      {/* Header */}
      <Card className="mb-3 sm:mb-4 p-3 sm:p-4 rounded-lg shadow-sm border border-border">
        <div className="flex items-center gap-3">
          <Avatar className="w-10 h-10 sm:w-12 sm:h-12 border-2 border-primary flex-shrink-0">
            {otherParticipant?.fotoPerfil ? (
              <AvatarImage src={otherParticipant.fotoPerfil} alt={displayName} />
            ) : (
              <AvatarFallback className="bg-primary text-primary-foreground text-sm sm:text-base font-semibold">
                {displayName.charAt(0).toUpperCase()}
              </AvatarFallback>
            )}
          </Avatar>
          <div className="flex-1 min-w-0">
            <h2 className="font-semibold text-card-foreground text-sm sm:text-base truncate">
              {displayName}
            </h2>
            <p className="text-xs sm:text-sm text-muted-foreground">Conversa ativa</p>
          </div>
        </div>
      </Card>

      {/* Messages Area */}
      <Card className="flex-1 overflow-hidden flex flex-col mb-3 sm:mb-4 p-0 shadow-sm border border-border">
        <div
          ref={messagesContainerRef}
          className="flex-1 overflow-y-auto px-3 sm:px-4 py-4 space-y-2 sm:space-y-3 scroll-smooth"
          style={{ scrollbarWidth: 'thin' }}
        >
          {!me || !me.email ? (
            <div className="text-center text-muted-foreground py-8">
              <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
              <p className="text-sm">Carregando...</p>
            </div>
          ) : allMessages.length === 0 ? (
            <div className="text-center text-muted-foreground py-12">
              <MessageCircle className="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p className="text-base font-medium mb-1">Nenhuma mensagem ainda</p>
              <p className="text-sm">Comece a conversa enviando uma mensagem!</p>
            </div>
          ) : (
            allMessages.map((m, idx) => {
              const prev = allMessages[idx - 1];
              const isMine = isMyMessage(m.senderId);

              // Determina se deve mostrar avatar
              const prevSenderId = prev ? normalizeEmail(prev.senderId) : "";
              const currentSenderId = normalizeEmail(m.senderId);
              const showAvatar = !prev || prevSenderId !== currentSenderId;

              // Determina se deve mostrar timestamp
              const timeDiff = prev
                ? new Date(m.createdAt).getTime() - new Date(prev.createdAt).getTime()
                : Infinity;
              const showTime = !prev || timeDiff > 300000; // 5 minutos

              // Espaçamento
              const marginTop = prev && prevSenderId === currentSenderId && !showTime
                ? "mt-1"
                : "mt-3";

              return (
                <div key={m.id} className="w-full">
                  {showTime && (
                    <div className="text-center text-xs text-muted-foreground mb-4 mt-4 font-medium">
                      {formatMessageDate(m.createdAt)}
                    </div>
                  )}
                  <div
                    className={`flex items-end gap-2 ${
                      isMine ? "flex-row-reverse" : "flex-row"
                    } ${marginTop}`}
                  >
                    {/* Avatar do outro participante - sempre reserva espaço */}
                    {!isMine && (
                      <div className="w-7 h-7 sm:w-8 sm:h-8 flex-shrink-0">
                        {showAvatar && (
                          <Avatar className="w-full h-full">
                            {otherParticipant?.fotoPerfil ? (
                              <AvatarImage src={otherParticipant.fotoPerfil} alt={displayName} />
                            ) : (
                              <AvatarFallback className="bg-muted text-muted-foreground text-xs font-semibold">
                                {displayName.charAt(0).toUpperCase()}
                              </AvatarFallback>
                            )}
                          </Avatar>
                        )}
                      </div>
                    )}

                    {/* Espaçador para mensagens minhas - sempre reserva espaço */}
                    {isMine && (
                      <div className="w-7 h-7 sm:w-8 sm:h-8 flex-shrink-0" />
                    )}

                    {/* Bala da mensagem */}
                    <div
                      className={`max-w-[75%] sm:max-w-[70%] rounded-2xl px-3 sm:px-4 py-2 break-words shadow-sm ${
                        isMine
                          ? "bg-primary text-primary-foreground rounded-br-sm"
                          : "bg-muted text-muted-foreground rounded-bl-sm"
                      } ${m.pending ? "opacity-70" : ""}`}
                    >
                      {/* Nome do remetente (apenas para mensagens do outro) */}
                      {!isMine && showAvatar && (
                        <p className="text-xs font-semibold mb-1 opacity-80">
                          {displayName}
                        </p>
                      )}

                      {/* Conteúdo da mensagem */}
                      <p className="text-sm sm:text-base leading-relaxed whitespace-pre-wrap">
                        {m.content}
                      </p>

                      {/* Timestamp e indicador de pendente */}
                      <div className="flex items-center justify-end gap-1.5 mt-1">
                        <p
                          className={`text-[10px] sm:text-xs ${
                            isMine
                              ? "text-primary-foreground/70"
                              : "text-muted-foreground/70"
                          }`}
                        >
                          {format(new Date(m.createdAt), "HH:mm")}
                        </p>
                        {m.pending && (
                          <div className="w-2.5 h-2.5 border-2 border-current border-t-transparent rounded-full animate-spin opacity-60" />
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>
      </Card>

      {/* Input Area */}
      <Card className="p-3 sm:p-4 rounded-lg shadow-sm border border-border">
        <div className="flex items-end gap-2">
          <div className="flex-1 relative">
            <Input
              value={content}
              onChange={(e) => setContent(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Digite sua mensagem..."
              className="min-h-[44px] pr-12 text-sm sm:text-base"
              disabled={isSending || !me}
            />
          </div>
          <Button
            onClick={sendMessage}
            disabled={!content.trim() || isSending || !me}
            size="icon"
            className="flex-shrink-0 h-[44px] w-[44px]"
            title="Enviar mensagem"
          >
            {isSending ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Send className="w-5 h-5" />
            )}
          </Button>
        </div>
      </Card>
    </div>
  );
}
