import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import api from "@/api/axios";

type ConversationPreview = {
  id: string;
  group: boolean;
  otherUserId?: string | null;
  otherUserName?: string | null;
  otherUserAvatarUrl?: string | null;
  unreadCount?: number;
  lastMessage?: {
    senderId: string;
    content: string;
    createdAt: string; // ISO
  } | null;
};

type UserLite = {
  id: number; // seu User usa Long
  email: string;
  username: string;
  fotoPerfil?: string | null;
};

export default function MessagesPage() {
  const { token } = useAuth();
  const nav = useNavigate();

  const [loadingConvs, setLoadingConvs] = useState(true);
  const [convs, setConvs] = useState<ConversationPreview[]>([]);
  const [q, setQ] = useState("");
  const [contacts, setContacts] = useState<UserLite[]>([]);
  const [loadingContacts, setLoadingContacts] = useState(false);

  const headers = useMemo(
    () => ({
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    }),
    [token]
  );

  // Carrega conversas (centro)
  useEffect(() => {
    (async () => {
      try {
        setLoadingConvs(true);
        const r = await api.get("/chat/conversations", { headers });
        const data = Array.isArray(r.data) ? r.data : r.data.content ?? [];
        setConvs(data);
      } catch (e) {
        console.error("Erro ao buscar conversas:", e);
      } finally {
        setLoadingConvs(false);
      }
    })();
  }, [headers]);

  // Busca contatos (direita)
  async function loadContacts(term?: string) {
    try {
      setLoadingContacts(true);
      const r = await api.get("/api/users/following", { headers });
      const data = r.data;

      // mapeia resultado (array puro ou paginado)
      let list: UserLite[] = (Array.isArray(data) ? data : data.content ?? []).map(
        (u: any) => ({
          id: u.id,
          email: u.email,
          username: u.username ?? "Surfista",
          fotoPerfil: u.fotoPerfil ?? null,
        })
      );

      // filtro no front pelo termo (username/email)
      if (term && term.trim()) {
        const t = term.trim().toLowerCase();
        list = list.filter(
          (u) =>
            u.username?.toLowerCase().includes(t) ||
            u.email?.toLowerCase().includes(t)
        );
      }

      setContacts(list);
    } catch (e) {
      console.error("Erro ao buscar contatos:", e);
    } finally {
      setLoadingContacts(false);
    }
  }

  useEffect(() => {
    loadContacts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Inicia/abre DM e navega
  async function startDM(otherUserEmail: string) {
    try {
      const r = await api.post(
        "/chat/dm",
        { otherUserId: otherUserEmail },
        { headers }
      );
      const data = r.data; // { conversationId }
      nav(`/chat/${data.conversationId}`);
    } catch (e) {
      console.error("Erro ao abrir DM:", e);
      alert("Não foi possível abrir a conversa.");
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-6">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold">Mensagens</h1>
        <div />
      </div>

      {/* Layout em 2 colunas: centro (conversas) + direita (contatos) */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        {/* Centro: últimas conversas */}
        <section className="md:col-span-2">
          {loadingConvs ? (
            <Card>
              <p>Carregando conversas…</p>
            </Card>
          ) : convs.length === 0 ? (
            <Card>
              <p className="text-slate-700">
                Você ainda não tem conversas. Procure um usuário e inicie um DM.
              </p>
            </Card>
          ) : (
            <div className="space-y-3">
              {convs.map((c) => (
                <ConversationRow
                  key={c.id}
                  conv={c}
                  onOpen={() => nav(`/chat/${c.id}`)}
                />
              ))}
            </div>
          )}
        </section>

        {/* Direita: lista de contatos com busca */}
        <aside className="md:col-span-1">
          <Card className="sticky top-4">
            <div className="mb-3">
              <input
                value={q}
                onChange={(e) => {
                  setQ(e.target.value);
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter") loadContacts(q);
                }}
                placeholder="Buscar usuários…"
                className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:ring-2 focus:ring-sky-300"
              />
              <div className="mt-2 flex gap-2">
                <button
                  onClick={() => loadContacts(q)}
                  className="rounded-lg bg-sky-600 px-3 py-1 text-white hover:bg-sky-700"
                >
                  Buscar
                </button>
                <button
                  onClick={() => {
                    setQ("");
                    loadContacts("");
                  }}
                  className="rounded-lg bg-slate-200 px-3 py-1 hover:bg-slate-300"
                >
                  Limpar
                </button>
              </div>
            </div>

            {loadingContacts ? (
              <p>Carregando contatos…</p>
            ) : contacts.length === 0 ? (
              <p className="text-slate-600">Nenhum usuário encontrado.</p>
            ) : (
              <ul className="max-h-[70vh] space-y-2 overflow-y-auto pr-1">
                {contacts.map((u) => (
                  <li key={u.id}>
                    <button
                      onClick={() => startDM(u.email)}
                      className="flex w-full items-center gap-3 rounded-lg p-2 hover:bg-slate-100"
                      title={`Conversar com ${u.username}`}
                    >
                      <Avatar src={u.fotoPerfil} alt={u.username} />
                      <div className="flex flex-1 flex-col text-left">
                        <span className="font-medium">{u.username}</span>
                        <span className="text-xs text-slate-500">
                          {u.email}
                        </span>
                      </div>
                      <span className="text-sky-700">Iniciar</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </aside>
      </div>
    </div>
  );
}

/* =============== componentes “bobos” =============== */

function Card({
  children,
  className = "",
}: {
  children: any;
  className?: string;
}) {
  return (
    <div
      className={`rounded-2xl border border-slate-200 bg-white p-4 shadow-sm ${className}`}
    >
      {children}
    </div>
  );
}

function Avatar({ src, alt }: { src?: string | null; alt?: string }) {
  return src ? (
    <img
      src={src}
      alt={alt ?? "avatar"}
      className="h-10 w-10 rounded-full object-cover"
    />
  ) : (
    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-slate-600">
      {(alt ?? "S").charAt(0).toUpperCase()}
    </div>
  );
}

function ConversationRow({
  conv,
  onOpen,
}: {
  conv: ConversationPreview;
  onOpen: () => void;
}) {
  const name =
    conv.otherUserName ?? (conv.group ? "Grupo" : conv.otherUserId ?? "Conversa");

  return (
    <button
      onClick={onOpen}
      className="flex w-full items-center gap-3 rounded-2xl border border-slate-200 bg-white p-3 text-left shadow-sm hover:bg-slate-50"
    >
      <Avatar src={conv.otherUserAvatarUrl ?? undefined} alt={name} />

      <div className="min-w-0 flex-1">
        <div className="flex items-center justify-between">
          <span className="truncate font-medium">{name}</span>
          {conv.unreadCount ? (
            <span className="ml-2 rounded-full bg-sky-600 px-2 py-0.5 text-xs text-white">
              {conv.unreadCount}
            </span>
          ) : null}
        </div>
        {conv.lastMessage ? (
          <p className="mt-0.5 line-clamp-1 text-sm text-slate-600">
            {conv.lastMessage.content}
          </p>
        ) : (
          <p className="mt-0.5 text-sm text-slate-400">Sem mensagens ainda</p>
        )}
      </div>
    </button>
  );
}
