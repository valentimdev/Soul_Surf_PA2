// src/services/chatApi.ts
const API = import.meta.env.VITE_API_URL || "http://localhost:8080";

function authHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    Authorization: token ? `Bearer ${token}` : "",
  };
}

export type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachmentUrl?: string | null;
  createdAt: string;
  editedAt?: string | null;
};

export async function listMessages(conversationId: string, page = 0, size = 30) {
  const res = await fetch(
    `${API}/api/chat/conversations/${conversationId}/messages?page=${page}&size=${size}`,
    { headers: authHeaders() }
  );
  if (!res.ok) throw new Error("Falha ao carregar mensagens");
  const data: ChatMessage[] = await res.json();
  // o backend retorna em ordem DESC; vamos inverter para mostrar ASC
  return data.slice().reverse();
}

export async function sendMessage(conversationId: string, content: string, attachmentUrl?: string | null) {
  const res = await fetch(`${API}/api/chat/conversations/${conversationId}/messages`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ content, attachmentUrl: attachmentUrl ?? null }),
  });
  if (!res.ok) throw new Error("Falha ao enviar mensagem");
  const data: ChatMessage = await res.json();
  return data;
}

// (opcional) criar/obter DM
export async function createOrGetDM(otherUserId: string) {
  const res = await fetch(`${API}/api/chat/dm`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ otherUserId }),
  });
  if (!res.ok) throw new Error("Falha ao criar/pegar DM");
  return res.json() as Promise<{ conversationId: string }>;
}
