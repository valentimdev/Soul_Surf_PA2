# 🌊 Soul Surf - Backend (Spring Boot)

<p align="center">
  <strong>Backend Spring Boot da plataforma Soul Surf — autenticação JWT, posts, comentários com menções, notificações, chat em tempo real (WebSocket) e integrações.</strong>
</p>

<p align="center">
  <a href="https://openjdk.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
  </a>
  <a href="https://spring.io/projects/spring-boot" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 3.3">
  </a>
  <a href="https://www.postgresql.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/PostgreSQL-DB-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  </a>
  <a href="https://jwt.io/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT">
  </a>
  <a href="https://maven.apache.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
  </a>
  <a href="https://stomp.github.io/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/WebSocket-STOMP-4E9A06?style=for-the-badge" alt="WebSocket STOMP">
  </a>
</p>

Backend da plataforma social de surfistas: autenticação JWT, posts, comentários com menções, notificações, chat (REST + WebSocket), perfis, praias e clima.

- Java 17 • Spring Boot 3 • Spring Security (JWT) • JPA • PostgreSQL • WebSocket (STOMP)
- Pasta: `backend/` (este repositório)

---

## ⚙️ Como Rodar (Local)

1) Configurar variáveis sensíveis em `application.properties` (DB, JWT, storage, e-mail).  
2) Rodar com Maven:

```bash
mvnw spring-boot:run
```

Backend fica disponível em: http://localhost:8080

Swagger (se ativo): http://localhost:8080/swagger-ui/index.html

---

## 🔐 Autenticação JWT

- Registro: `POST /api/auth/signup` (email, username, password)
- Login: `POST /api/auth/login` → retorna `{ token }`
- Header obrigatório: `Authorization: Bearer {token}`

Exemplo Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "surfista@mail.com",
  "password": "123456"
}
```

Resposta:
```json
{ "token": "eyJhbGciOi..." }
```

Como saber se o usuário é ADMIN (Front-End):
- O token JWT não carrega a role no payload. Utilize uma destas abordagens:
  1) Checagem de permissão: tente `GET /api/admin/metrics`. Se 200 → admin; se 403 → não admin.
  2) Opcional: exiba telas admin apenas após resposta positiva do endpoint acima.

---

## 🚀 Como o Front-End Usa (Simples e Direto)

### 1) Feed de Posts

- Feed público: `GET /api/posts/home` (requer JWT)
- Feed “seguindo”: `GET /api/posts/following` (requer JWT)
- Post por ID: `GET /api/posts/{id}` (se privado, só o dono vê)

Criar Post (multipart):
```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: multipart/form-data

publico=true
descricao="Dia épico em Taíba"
foto=<arquivo opcional>
beachId=123 (opcional)
```

Editar Post:
```http
PUT /api/posts/{id}
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

descricao="Atualizando a legenda"
```

---

### 2) Comentários (+ Respostas)

Base: `/api/posts/{postId}/comments`

- Listar comentários: `GET /`  
- Criar comentário: `POST /` (requer JWT)  
  Parâmetros: `texto` e opcional `parentId` (para resposta)
- Editar: `PUT /{commentId}` (requer JWT)
- Remover: `DELETE /{commentId}` (requer JWT)

Criar comentário (com possível resposta):
```http
POST /api/posts/42/comments/
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

texto="Muito bom @joao_surfista"
parentId=128   # opcional (quando é resposta)
```

---

### 3) Notificações (Menção, Comentário, Resposta)

Base: `/api/notifications` (requer JWT)

- Buscar notificações: `GET /`  
- Contar não lidas: `GET /count`  
- Marcar como lida: `PUT /{id}/read`

Criar (para o front disparar após ações):
- Menção: `POST /mention?recipientUsername={user}&postId={id}&commentId={id}`
- Comentário: `POST /comment?postId={id}&commentId={id}`
- Resposta: `POST /reply?postId={id}&commentId={id}&parentCommentId={id}`

Exemplo sequência ao criar comentário com menção no front:
```javascript
// 1) Criar comentário (vide seção comentários)
const comment = await createComment(postId, texto);

// 2) Notificar dono do post
await fetch(`/api/notifications/comment?postId=${postId}&commentId=${comment.id}`, {
  method: 'POST', headers: { Authorization: `Bearer ${token}` }
});

// 3) Detectar menções e notificar
for (const username of (texto.match(/@(\w+)/g) || []).map(s => s.slice(1))) {
  await fetch(`/api/notifications/mention?recipientUsername=${username}&postId=${postId}&commentId=${comment.id}`, {
    method: 'POST', headers: { Authorization: `Bearer ${token}` }
  });
}
```

Mensagens geradas automaticamente no DTO:  
- MENTION → "{user} mencionou você em um comentário"
- COMMENT → "{user} comentou em seu post"
- REPLY → "{user} respondeu ao seu comentário"

Regra de ruído: o backend evita notificar quando é ação sobre si mesmo (auto-menção, comentar no próprio post, responder a si).

---

### 4) Sugestões de Menções (@username) - Autocomplete

Endpoint: `GET /api/users/mention-suggestions?query={texto}&limit=5` (requer JWT)  
Prioriza usuários que o cliente segue, depois completa com demais.

Exemplo:
```javascript
const getSuggestions = async (searchTerm) => {
  const r = await fetch(`/api/users/mention-suggestions?query=${searchTerm}&limit=5`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return r.json();
};
```

Resposta típica:
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
- Atualizar perfil (com arquivos): `PUT /me/upload` (multipart)
- Seguir usuário: `POST /{id}/follow` (requer JWT)
- Deixar de seguir: `DELETE /{id}/follow` (requer JWT)
- Quem eu sigo: `GET /following` (requer JWT)
- Quem o usuário segue: `GET /{id}/following`
- Seguidores do usuário: `GET /{id}/followers`

Atualizar perfil com arquivos:
```http
PUT /api/users/me/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

username=novo_nome
bio="Sobre mim..."
fotoPerfil=<arquivo>
fotoCapa=<arquivo>
```

---

### 6) Chat (DM) + WebSocket

Base REST: `/api/chat` (requer JWT)

- Criar/obter DM: `POST /dm` body `{ otherUserId }` → `{ conversationId }`
- Minhas conversas: `GET /conversations` → lista com preview e unreadCount
- Mensagens da conversa: `GET /conversations/{id}/messages?page=0&size=30`
- Enviar mensagem: `POST /conversations/{id}/messages` body `{ content, attachmentUrl }`

WebSocket (STOMP):
- Endpoint handshake: `/ws` (SockJS habilitado)
- Broker: subscribe em `/topic/conversations/{conversationId}` para receber mensagens

Exemplo subscribe (front):
```javascript
client.subscribe(`/topic/conversations/${conversationId}`, (frame) => {
  const msg = JSON.parse(frame.body);
  // renderizar mensagem
});
```

---

### 7) Upload de Arquivos

Base: `/api/files` (requer JWT)

- Upload: `POST /upload` (multipart) → retorna URL pública
- Listagem: `GET /list` → lista de URLs

Exemplo upload:
```http
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file=<arquivo>
```

---

### 8) Praias (Beaches)

Base: `/api/beaches`

- Listar praias: `GET /`
- Detalhe: `GET /{id}`
- Criar praia: `POST /` (requer JWT)
- Posts públicos por praia: `GET /{id}/posts?page=0&size=20`
- Posts (admin, inclui privados): `GET /{id}/all-posts` (requer ADMIN)

---

### 9) Mural da Praia (Mensagens Públicas)

Base: `/api/beaches/{beachId}/mensagens`

- Listar mensagens: `GET /`
- Postar mensagem: `POST /` (requer JWT) — parâmetro `texto`

---

### 10) Clima (OpenWeather)

Base: `/api/weather`

- Atual: `GET /current?city=Fortaleza,BR` (público)

---

### 11) Administração (ADMIN)

Base: `/api/admin` (requer ADMIN)

- Apagar usuário: `DELETE /users/{userId}`
- Apagar post: `DELETE /posts/{postId}`
- Apagar comentário: `DELETE /comments/{commentId}`
- Promover admin: `POST /users/{userId}/promote`
- Remover admin: `POST /users/{userId}/demote`
- Banir usuário: `POST /users/{userId}/ban`
- Desbanir: `POST /users/{userId}/unban`
- Auditorias: `GET /audits?page=0&size=20`
- Métricas: `GET /metrics`
- Métricas por período: `GET /metrics/period?start=YYYY-MM-DDTHH:mm:ss&end=YYYY-MM-DDTHH:mm:ss`
- Top autores: `GET /metrics/top-authors?start=...&end=...&limit=10`
- Posts por praia: `GET /metrics/by-beach?start=...&end=...`

Checagem de admin no front (leve):
```javascript
async function isAdmin(token){
  const r = await fetch('/api/admin/metrics', { headers: { Authorization: `Bearer ${token}` }});
  return r.status === 200;
}
```

---

## ✅ Regras e Observações Importantes

- Endpoints protegidos exigem `Authorization: Bearer {token}`.
- Notificações evitam auto-notificação (auto-menção, comentar no próprio post, responder a si).
- Sugestões de menção priorizam usuários que o cliente segue.
- WebSocket: use `/topic/conversations/{id}` para receber novas mensagens em tempo real.
- CORS: por padrão, `http://localhost:5173` está permitido (ajuste em produção).

---

## 📦 Estrutura (Resumo)

```
src/main/java/com/soulsurf/backend/
  config/ (WebSecurity, WebSocket)
  controllers/ (Auth, Users, Posts, Comments, Beaches, Mensagens, Files, Chat, Notifications, Admin, Weather)
  dto/ (UserDTO, PostDTO, CommentDTO, NotificationDTO, ...)
  entities/ (User, Post, Comment, Notification, ...)
  repository/ (...)
  security/ (JWT, filtros, UserDetails, AuthUtils, WebSocket interceptor)
  services/ (...)
resources/
  application.properties
```

---

## 🧪 Smoke Test Rápido

1) Registre-se (`/api/auth/signup`) e faça login (`/api/auth/login`).  
2) Crie um post (`/api/posts`).  
3) Comente com `@username` e dispare as notificações (`/api/notifications/comment` e `/mention`).  
4) Consulte notificações (`/api/notifications/` e `/count`).  
5) Abra um DM (`/api/chat/dm`) e assine `/topic/conversations/{id}`; envie mensagem e verifique recebimento.

Pronto. O front já consegue consumir tudo com segurança e sem surpresas.
