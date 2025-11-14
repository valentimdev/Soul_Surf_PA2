# 🏄 Soul Surf - Frontend (React + TypeScript)

<p align="center">
  <strong>Frontend React/TypeScript da plataforma Soul Surf — consumo do backend Spring Boot, autenticação JWT, feed de posts, menções, notificações em tempo real e chat via WebSocket.</strong>
</p>

<p align="center">
  <a href="https://www.typescriptlang.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  </a>
  <a href="https://react.dev/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React">
  </a>
  <a href="https://vitejs.dev/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Vite-5.x-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite">
  </a>
  <a href="https://tailwindcss.com/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Tailwind-CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind">
  </a>
  <a href="https://axios-http.com/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Axios-HTTP-5A29E4?style=for-the-badge&logo=axios&logoColor=white" alt="Axios">
  </a>
  <a href="https://stomp-js.github.io/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/WebSocket-STOMP-4E9A06?style=for-the-badge" alt="WebSocket STOMP">
  </a>
</p>

Frontend da plataforma social de surfistas: autenticação JWT, feed de posts, comentários com menções, notificações, chat em tempo real (WebSocket), perfis, praias e clima.

- React 18 • TypeScript • Vite • Tailwind CSS • Axios • WebSocket (STOMP) • SockJS
- Pasta: `front_end/` (este repositório)

---

## ⚙️ Como Rodar (Local)

1) Instalar dependências:

```bash
npm install
```

2) Configurar variáveis de ambiente (opcional, criar `.env` na raiz do `front_end/`):

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
```

3) Rodar em modo desenvolvimento:

```bash
npm run dev
```

Frontend fica disponível em: http://localhost:5173

Build para produção:

```bash
npm run build
npm run preview  # preview local do build
```

---

## 🔐 Autenticação JWT

- Registro: `POST /api/auth/signup` (email, username, password)
- Login: `POST /api/auth/login` → retorna `{ token }`
- Token armazenado em: `localStorage.getItem('token')`
- Header obrigatório em todas requisições protegidas: `Authorization: Bearer {token}`

Exemplo Login (código):

```typescript
const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { token } = await response.json();
localStorage.setItem('token', token);
```

Como saber se o usuário é ADMIN:
```typescript
async function isAdmin(token: string): Promise<boolean> {
  try {
    const response = await fetch('/api/admin/metrics', {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.status === 200;
  } catch {
    return false;
  }
}
```

---

## 🚀 Como o Frontend Usa o Backend (Endpoints)

Base API: `VITE_API_BASE_URL` (padrão: http://localhost:8080)

### 1) Feed de Posts

- Feed público: `GET /api/posts/home` (requer JWT)
- Feed "seguindo": `GET /api/posts/following` (requer JWT)
- Post por ID: `GET /api/posts/{id}`

Criar Post (multipart):
```typescript
const formData = new FormData();
formData.append('publico', 'true');
formData.append('descricao', 'Dia épico em Taíba');
formData.append('foto', file); // opcional
formData.append('beachId', '123'); // opcional

await fetch('/api/posts', {
  method: 'POST',
  headers: { Authorization: `Bearer ${token}` },
  body: formData
});
```

Editar Post:
```typescript
const params = new URLSearchParams();
params.append('descricao', 'Nova descrição');

await fetch(`/api/posts/${postId}`, {
  method: 'PUT',
  headers: {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  body: params
});
```

---

### 2) Comentários (+ Respostas)

Base: `/api/posts/{postId}/comments`

- Listar comentários: `GET /`
- Criar comentário: `POST /` (params: `texto`, opcional `parentId`)
- Editar: `PUT /{commentId}`
- Remover: `DELETE /{commentId}`

Criar comentário com menção:
```typescript
const params = new URLSearchParams();
params.append('texto', 'Muito bom @joao_surfista');
params.append('parentId', '128'); // opcional (resposta)

const response = await fetch(`/api/posts/${postId}/comments/`, {
  method: 'POST',
  headers: {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  body: params
});
const comment = await response.json();
```

---

### 3) Notificações (Menção, Comentário, Resposta)

Base: `/api/notifications` (requer JWT)

- Buscar notificações: `GET /`
- Contar não lidas: `GET /count`
- Marcar como lida: `PUT /{id}/read`

Criar notificações (chamar após ações):
- Menção: `POST /mention?recipientUsername={user}&postId={id}&commentId={id}`
- Comentário: `POST /comment?postId={id}&commentId={id}`
- Resposta: `POST /reply?postId={id}&commentId={id}&parentCommentId={id}`

Fluxo completo ao criar comentário com menção:
```typescript
// 1) Criar comentário
const comment = await createComment(postId, texto);

// 2) Notificar dono do post
await fetch(`/api/notifications/comment?postId=${postId}&commentId=${comment.id}`, {
  method: 'POST',
  headers: { Authorization: `Bearer ${token}` }
});

// 3) Detectar menções (@username) e notificar
const mentions = texto.match(/@(\w+)/g) || [];
for (const mention of mentions) {
  const username = mention.slice(1);
  await fetch(
    `/api/notifications/mention?recipientUsername=${username}&postId=${postId}&commentId=${comment.id}`,
    { method: 'POST', headers: { Authorization: `Bearer ${token}` } }
  );
}
```

Mensagens geradas automaticamente:
- MENTION → "{user} mencionou você em um comentário"
- COMMENT → "{user} comentou em seu post"
- REPLY → "{user} respondeu ao seu comentário"

---

### 4) Sugestões de Menções (@username) - Autocomplete

Endpoint: `GET /api/users/mention-suggestions?query={texto}&limit=5` (requer JWT)

Retorna usuários priorizando quem você segue.

Exemplo (com debounce):
```typescript
const getSuggestions = async (searchTerm: string) => {
  if (searchTerm.length < 2) return [];
  
  const response = await fetch(
    `/api/users/mention-suggestions?query=${searchTerm}&limit=5`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.json();
};

// Usar com debounce de 300ms
const debouncedSearch = debounce(getSuggestions, 300);
```

Resposta:
```json
[
  { "id": 1, "username": "joao_surfista", "fotoPerfil": "https://..." },
  { "id": 5, "username": "joaquim_beach", "fotoPerfil": "https://..." }
]
```

---

### 5) Perfis e Social (seguir/deixar de seguir)

Base: `/api/users`

- Meu perfil: `GET /me` (requer JWT)
- Perfil por ID: `GET /{id}`
- Atualizar perfil: `PUT /me/upload` (multipart)
- Seguir usuário: `POST /{id}/follow`
- Deixar de seguir: `DELETE /{id}/follow`
- Quem eu sigo: `GET /following`
- Quem o usuário segue: `GET /{id}/following`
- Seguidores: `GET /{id}/followers`

Atualizar perfil com fotos:
```typescript
const formData = new FormData();
formData.append('username', 'novo_nome');
formData.append('bio', 'Sobre mim...');
formData.append('fotoPerfil', fotoPerfilFile);
formData.append('fotoCapa', fotoCapaFile);

await fetch('/api/users/me/upload', {
  method: 'PUT',
  headers: { Authorization: `Bearer ${token}` },
  body: formData
});
```

---

### 6) Chat (DM) + WebSocket

Base REST: `/api/chat` (requer JWT)

- Criar/obter DM: `POST /dm` body `{ otherUserId }`
- Minhas conversas: `GET /conversations`
- Mensagens: `GET /conversations/{id}/messages?page=0&size=30`
- Enviar mensagem: `POST /conversations/{id}/messages` body `{ content, attachmentUrl }`

WebSocket (STOMP):
- Endpoint: `{VITE_WS_URL}` (ex.: http://localhost:8080/ws)
- Subscribe: `/topic/conversations/{conversationId}`

Exemplo completo:
```typescript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS(VITE_WS_URL);
const client = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    client.subscribe(`/topic/conversations/${conversationId}`, (message) => {
      const msg = JSON.parse(message.body);
      // Renderizar nova mensagem
      addMessageToUI(msg);
    });
  }
});

client.activate();
```

---

### 7) Upload de Arquivos

Base: `/api/files` (requer JWT)

- Upload: `POST /upload` (multipart) → retorna URL pública
- Listagem: `GET /list`

Exemplo:
```typescript
const formData = new FormData();
formData.append('file', file);

const response = await fetch('/api/files/upload', {
  method: 'POST',
  headers: { Authorization: `Bearer ${token}` },
  body: formData
});

const { url } = await response.json();
```

---

### 8) Praias (Beaches)

Base: `/api/beaches`

- Listar praias: `GET /`
- Detalhe: `GET /{id}`
- Criar praia: `POST /` (requer JWT)
- Posts públicos por praia: `GET /{id}/posts?page=0&size=20`

---

### 9) Mural da Praia (Mensagens Públicas)

Base: `/api/beaches/{beachId}/mensagens`

- Listar mensagens: `GET /`
- Postar mensagem: `POST /` (param: `texto`)

---

### 10) Clima (OpenWeather)

Base: `/api/weather`

- Clima atual: `GET /current?city=Fortaleza,BR` (público)

```typescript
const response = await fetch('/api/weather/current?city=Fortaleza,BR');
const weather = await response.json();
```

---

### 11) Administração (ADMIN)

Base: `/api/admin` (requer ADMIN)

- Apagar usuário: `DELETE /users/{userId}`
- Apagar post: `DELETE /posts/{postId}`
- Apagar comentário: `DELETE /comments/{commentId}`
- Promover admin: `POST /users/{userId}/promote`
- Banir usuário: `POST /users/{userId}/ban`
- Desbanir: `POST /users/{userId}/unban`
- Métricas: `GET /metrics`
- Top autores: `GET /metrics/top-authors?start=...&end=...&limit=10`

---

## 📦 Estrutura do Projeto (Resumo)

```
front_end/
  public/
    (assets estáticos)
  src/
    api/
      axios.ts              # configuração axios
      routes/               # endpoints organizados
        auth.ts
        beach.ts
        post.ts
        user.ts
        file.ts
        mencoes.ts
    assets/                 # imagens, ícones
    components/
      customCards/          # Cards customizados
      pages/                # componentes de página
      ui/                   # componentes UI reutilizáveis
    contexts/
      AuthContext.tsx       # contexto de autenticação
    hooks/
      useAuth.ts            # hook de autenticação
    layouts/
      Header.tsx
      SideBarLeft.tsx
      SideBarRight.tsx
      RootLayout.tsx
    lib/
      api.ts                # configuração base API
      utils.ts              # funções utilitárias
    pages/
      HomePage.tsx
      LoginPage.tsx
      ProfilePage.tsx
      ChatPage.tsx
      BeachDetailPage.tsx
      (...)
    services/
      authService.ts
      postService.ts
      userService.ts
      chatSocket.ts         # WebSocket STOMP
      notificationService.ts
      (...)
    App.tsx
    main.tsx
    index.css
  .env                      # variáveis de ambiente
  package.json
  vite.config.ts
  tsconfig.json
```

---

## 🧭 CORS & Configurações

- Dev server: http://localhost:5173 (padrão do Vite)
- Backend deve permitir este origin (já configurado em `WebSecurityConfig`)
- Variáveis de ambiente principais:
  - `VITE_API_BASE_URL` → URL base do backend (ex.: http://localhost:8080)
  - `VITE_WS_URL` → URL WebSocket (ex.: http://localhost:8080/ws)

---

## ✅ Regras e Observações Importantes

- Token JWT armazenado em `localStorage` — considerar refresh token em produção
- Notificações: evitar notificar o próprio usuário (backend já trata)
- Autocomplete de menções: usar debounce de 300ms para evitar spam de requisições
- WebSocket: implementar reconexão automática em caso de queda
- Upload de arquivos: validar tamanho e tipo no frontend antes de enviar
- Imagens: usar lazy loading para melhor performance
- Rotas protegidas: usar `ProtectedRoute` component para verificar autenticação

---

## 🧪 Smoke Test Rápido (Frontend + Backend)

1) Configurar `.env` com `VITE_API_BASE_URL=http://localhost:8080`
2) Rodar backend (`mvnw spring-boot:run`) e frontend (`npm run dev`)
3) Acessar http://localhost:5173
4) Registrar usuário e fazer login
5) Criar um post com foto
6) Comentar com `@username` (testar autocomplete)
7) Verificar notificações (badge de count)
8) Abrir chat/DM, enviar mensagem e verificar recepção em tempo real
9) Visitar página de praia e ver posts/clima
10) Testar upload de foto de perfil

---

## 🛠️ Scripts Disponíveis

```bash
npm run dev          # Roda em modo desenvolvimento
npm run build        # Build para produção
npm run preview      # Preview do build de produção
npm run lint         # Lint do código
```

---

## 🎨 Bibliotecas Principais

- **React Router DOM** — roteamento
- **Axios** — requisições HTTP
- **@stomp/stompjs + sockjs-client** — WebSocket
- **Tailwind CSS** — estilização
- **Radix UI** — componentes acessíveis (dropdown, dialog, etc.)
- **Lucide React** — ícones
- **React Hook Form** (opcional) — formulários
- **Zod** (opcional) — validação

---

## 📝 Boas Práticas Implementadas

- Context API para gerenciamento de estado de autenticação
- Services separados para chamadas ao backend
- Custom hooks para lógica reutilizável
- Componentes atômicos e reutilizáveis
- TypeScript para type safety
- Lazy loading de imagens
- Error boundaries para tratamento de erros
- Debounce em buscas e autocomplete
- Feedback visual em ações assíncronas (loading states)

---

## 🚀 Próximos Passos

- [ ] Implementar service worker para PWA
- [ ] Adicionar testes unitários (Vitest)
- [ ] Implementar refresh token automático
- [ ] Adicionar internacionalização (i18n)
- [ ] Melhorar SEO com meta tags dinâmicas
- [ ] Implementar cache de requisições
- [ ] Adicionar analytics

---

## 🤝 Integração com Backend

Este frontend consome a API REST do backend Spring Boot. Certifique-se de que:

1) Backend está rodando em `http://localhost:8080`
2) CORS está configurado para permitir `http://localhost:5173`
3) Variáveis de ambiente estão configuradas corretamente
4) Token JWT é enviado em todas requisições protegidas

Veja o README do backend em: `../backend/README.md`

---

**🏄 Soul Surf — Conectando surfistas através da tecnologia! 🌊**

