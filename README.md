# 🌊 Soul Surf - Plataforma Social para Surfistas

<p align="center">
  <strong>Rede social completa para surfistas — compartilhe experiências, descubra praias, conecte-se com a comunidade e acompanhe condições climáticas em tempo real.</strong>
</p>

<p align="center">
  <a href="https://openjdk.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
  </a>
  <a href="https://spring.io/projects/spring-boot" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot">
  </a>
  <a href="https://www.postgresql.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/PostgreSQL-DB-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  </a>
  <a href="https://react.dev/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React">
  </a>
  <a href="https://www.typescriptlang.org/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  </a>
  <a href="https://www.terraform.io/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Terraform-IaC-844FBA?style=for-the-badge&logo=terraform&logoColor=white" alt="Terraform">
  </a>
  <a href="https://www.ansible.com/" target="_blank" rel="noopener noreferrer">
    <img src="https://img.shields.io/badge/Ansible-Deploy-EE0000?style=for-the-badge&logo=ansible&logoColor=white" alt="Ansible">
  </a>
</p>

---

## 📋 Sobre o Projeto

**Soul Surf** é uma plataforma social completa desenvolvida para conectar surfistas, compartilhar experiências, descobrir praias e acompanhar condições climáticas em tempo real.

Projeto desenvolvido para a disciplina **Projeto Aplicado 2** — continuação e evolução do protótipo SoulSurf original.

### ✨ Principais Funcionalidades

- 🔐 **Autenticação JWT** com Spring Security
- 📱 **Feed de Posts** (público/privado) com fotos
- 💬 **Comentários Aninhados** (respostas a comentários)
- 🏷️ **Sistema de Menções** (@username) com autocomplete inteligente
- 🔔 **Notificações em Tempo Real** (menções, comentários, respostas)
- 💬 **Chat/DM** com WebSocket (STOMP) para mensagens instantâneas
- 👥 **Sistema Social** (seguir/deixar de seguir, perfis públicos)
- 🏖️ **Catálogo de Praias** com posts por localização
- 🗨️ **Mural de Praias** (mensagens públicas da comunidade)
- ☀️ **Integração com OpenWeather** (clima em tempo real)
- 📤 **Upload de Arquivos** (fotos de perfil, capa, posts)
- 👮 **Painel Administrativo** (métricas, auditoria, moderação)
- 🚀 **Infraestrutura como Código** (Terraform + Ansible)

---

## 🏗️ Arquitetura do Projeto

```
Soul_Surf_PA2/
├── backend/              # API REST Spring Boot + WebSocket
├── front_end/            # Interface React + TypeScript
├── infrastructure/       # Terraform (IaC)
├── ansible/              # Automação de deploy
└── README.md            # Este arquivo
```

### 🔧 Stack Tecnológica

#### Backend
- **Java 17** + **Spring Boot 3.3**
- **Spring Security** (JWT)
- **Spring Data JPA** + **PostgreSQL**
- **WebSocket (STOMP)** para chat em tempo real
- **Maven** para build
- **OpenWeather API** para clima

#### Frontend
- **React 18** + **TypeScript**
- **Vite** (build tool)
- **Tailwind CSS** para estilização
- **Axios** para requisições HTTP
- **@stomp/stompjs + SockJS** para WebSocket
- **Radix UI** para componentes acessíveis

#### Infraestrutura
- **Terraform** para provisionamento de infraestrutura
- **Ansible** para automação de deploy e configuração
- **PostgreSQL** como banco de dados

---

## ⚡ Quick Start

### Pré-requisitos

- **Java 17+** (backend)
- **Node.js 18+** e **npm** (frontend)
- **PostgreSQL** (banco de dados)
- **Maven** (gerenciamento de dependências Java)
- **Git** (controle de versão)

### 1️⃣ Clonar o Repositório

```bash
git clone <repo-url>
cd Soul_Surf_PA2
```

### 2️⃣ Configurar e Rodar o Backend

```bash
cd backend

# Configurar application.properties com credenciais do DB, JWT, etc.
# Editar: src/main/resources/application.properties

# Rodar aplicação
mvnw spring-boot:run
```

Backend disponível em: **http://localhost:8080**

📖 [Documentação completa do Backend](./backend/README.md)

### 3️⃣ Configurar e Rodar o Frontend

```bash
cd front_end

# Instalar dependências
npm install

# Configurar variáveis de ambiente (opcional)
# Criar arquivo .env com:
# VITE_API_BASE_URL=http://localhost:8080
# VITE_WS_URL=http://localhost:8080/ws

# Rodar em desenvolvimento
npm run dev
```

Frontend disponível em: **http://localhost:5173**

📖 [Documentação completa do Frontend](./front_end/README.md)

---

## 🔐 Autenticação e Segurança

- **JWT (JSON Web Token)** para autenticação stateless
- **Spring Security** com filtros personalizados
- **BCrypt** para hash de senhas
- **CORS** configurado para dev/prod
- **Roles** (USER, ADMIN) para controle de acesso
- **WebSocket Authentication** via token JWT

Fluxo básico:
1. Usuário faz login → recebe token JWT
2. Token armazenado no `localStorage` (frontend)
3. Todas requisições incluem header `Authorization: Bearer {token}`
4. Backend valida token e extrai usuário autenticado

---

## 📡 Comunicação em Tempo Real (WebSocket)

- **Protocolo**: STOMP sobre SockJS
- **Endpoint**: `/ws`
- **Uso**: Chat/DM, notificações (futuro)

Exemplo de conexão:
```typescript
const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: { Authorization: `Bearer ${token}` },
  onConnect: () => {
    client.subscribe('/topic/conversations/123', (msg) => {
      // Processar mensagem em tempo real
    });
  }
});
```

---

## 🗂️ Principais Endpoints da API

| Categoria | Endpoint | Método | Descrição |
|-----------|----------|--------|-----------|
| **Auth** | `/api/auth/signup` | POST | Registrar usuário |
| | `/api/auth/login` | POST | Login (retorna JWT) |
| **Posts** | `/api/posts/home` | GET | Feed público |
| | `/api/posts/following` | GET | Feed de quem você segue |
| | `/api/posts` | POST | Criar post |
| **Comentários** | `/api/posts/{id}/comments` | GET | Listar comentários |
| | `/api/posts/{id}/comments` | POST | Criar comentário/resposta |
| **Notificações** | `/api/notifications` | GET | Minhas notificações |
| | `/api/notifications/count` | GET | Contar não lidas |
| | `/api/notifications/mention` | POST | Notificar menção |
| **Usuários** | `/api/users/me` | GET | Meu perfil |
| | `/api/users/{id}/follow` | POST | Seguir usuário |
| | `/api/users/mention-suggestions` | GET | Autocomplete de menções |
| **Chat** | `/api/chat/dm` | POST | Criar/obter DM |
| | `/api/chat/conversations` | GET | Minhas conversas |
| | `/api/chat/conversations/{id}/messages` | GET | Mensagens |
| **Praias** | `/api/beaches` | GET | Listar praias |
| | `/api/beaches/{id}/posts` | GET | Posts por praia |
| | `/api/beaches/{id}/mensagens` | GET/POST | Mural da praia |
| **Clima** | `/api/weather/current` | GET | Clima atual (city) |
| **Admin** | `/api/admin/metrics` | GET | Métricas da plataforma |
| | `/api/admin/users/{id}/ban` | POST | Banir usuário |

📖 Veja documentação completa de endpoints em [Backend README](./backend/README.md) e [Frontend README](./front_end/README.md)

---

## 🎯 Fluxos Principais

### 1. Criar Post com Foto
```
Frontend → POST /api/posts (multipart/form-data)
  ├─ publico: true
  ├─ descricao: "Dia épico!"
  ├─ foto: arquivo.jpg
  └─ beachId: 42

Backend → Salvar arquivo → Salvar post no DB → Retornar PostDTO
```

### 2. Comentar com Menção (@username)
```
Frontend:
  1. Usuário digita "@jo" → buscar sugestões (GET /api/users/mention-suggestions?query=jo)
  2. Seleciona "@joao_surfista" → criar comentário (POST /api/posts/{id}/comments)
  3. Disparar notificação de menção (POST /api/notifications/mention?recipientUsername=joao_surfista&...)
  4. Disparar notificação ao dono do post (POST /api/notifications/comment?...)

Backend → Salvar comentário → Criar notificações → Retornar CommentDTO
```

### 3. Chat em Tempo Real
```
Frontend:
  1. Criar/obter conversa (POST /api/chat/dm com otherUserId)
  2. Conectar WebSocket (SockJS + STOMP)
  3. Subscribe em /topic/conversations/{conversationId}
  4. Enviar mensagem (POST /api/chat/conversations/{id}/messages)

Backend:
  → Salvar mensagem no DB
  → Broadcast via WebSocket para /topic/conversations/{conversationId}
  
Frontend: Recebe mensagem via WebSocket → Renderiza em tempo real
```

---

## 🚀 Deploy e Infraestrutura

### Terraform (IaC)

Provisionamento automatizado de infraestrutura na nuvem:

```bash
cd infrastructure

# Inicializar
terraform init

# Planejar mudanças
terraform plan

# Aplicar infraestrutura
terraform apply
```

Arquivos principais:
- `main.tf` — Configuração principal
- `backend.tf` — Remote state
- `variables.tf` — Variáveis configuráveis
- `backend-deploy.tf` — Deploy do backend
- `frontend-deploy.tf` — Deploy do frontend

### Ansible (Automação)

Automação de deploy e configuração de servidores:

```bash
cd ansible

# Deploy completo (backend + frontend)
ansible-playbook -i inventory/hosts.yml playbooks/site.yml

# Deploy apenas backend
ansible-playbook -i inventory/hosts.yml playbooks/deploy-backend.yml

# Deploy apenas frontend
ansible-playbook -i inventory/hosts.yml playbooks/deploy-frontend.yml

# Instalar dependências
ansible-playbook -i inventory/hosts.yml playbooks/install-dependencies.yml
```

---

## 🧪 Testes

### Smoke Test Completo

1. **Backend**:
   - Rodar `mvnw spring-boot:run`
   - Verificar http://localhost:8080/actuator/health

2. **Frontend**:
   - Rodar `npm run dev`
   - Acessar http://localhost:5173

3. **Fluxo E2E**:
   - Registrar usuário → Login
   - Criar post com foto
   - Comentar com menção (@username)
   - Verificar notificações
   - Abrir chat/DM
   - Enviar mensagem em tempo real
   - Visitar página de praia
   - Ver clima da praia

---

## 📊 Funcionalidades Administrativas

Painel admin com:
- 📈 **Métricas**: total de usuários, posts, comentários, conversas
- 📊 **Análises por Período**: filtrar por data
- 🏆 **Top Autores**: usuários mais ativos
- 🏖️ **Posts por Praia**: estatísticas de localização
- 🔍 **Auditoria**: log de ações administrativas
- 🚫 **Moderação**: banir/desbanir usuários, remover conteúdo
- 👑 **Gerenciamento**: promover/remover admins

Acesso: apenas usuários com role `ADMIN`

---

## 🛡️ Segurança e Boas Práticas

- ✅ Senhas hasheadas com BCrypt
- ✅ JWT com expiração configurável
- ✅ CORS configurado adequadamente
- ✅ Validação de entrada em DTOs
- ✅ Proteção contra SQL Injection (JPA)
- ✅ WebSocket autenticado via JWT
- ✅ Upload de arquivos com validação
- ✅ Rate limiting (recomendado para produção)
- ✅ HTTPS obrigatório em produção
- ✅ Auditoria de ações administrativas

---

## 📁 Estrutura Detalhada

```
Soul_Surf_PA2/
│
├── backend/                      # Spring Boot API
│   ├── src/main/java/com/soulsurf/backend/
│   │   ├── config/              # WebSecurity, WebSocket, CORS
│   │   ├── controllers/         # REST endpoints
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── entities/            # JPA entities
│   │   ├── repository/          # Spring Data repositories
│   │   ├── security/            # JWT, filters, auth
│   │   └── services/            # Business logic
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── pom.xml
│   └── README.md
│
├── front_end/                    # React + TypeScript
│   ├── src/
│   │   ├── api/                 # Axios config + routes
│   │   ├── components/          # UI components
│   │   │   ├── customCards/    # Cards personalizados
│   │   │   ├── pages/          # Componentes de página
│   │   │   └── ui/             # Componentes reutilizáveis
│   │   ├── contexts/           # React Context (Auth)
│   │   ├── hooks/              # Custom hooks
│   │   ├── layouts/            # Header, Sidebars, Layout
│   │   ├── pages/              # Páginas principais
│   │   ├── services/           # Services (API, WebSocket)
│   │   └── lib/                # Utils, config
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   └── README.md
│
├── infrastructure/               # Terraform IaC
│   ├── main.tf
│   ├── backend-deploy.tf
│   ├── frontend-deploy.tf
│   ├── variables.tf
│   └── outputs.tf
│
├── ansible/                      # Automação de deploy
│   ├── ansible.cfg
│   ├── inventory/
│   │   └── hosts.yml
│   └── playbooks/
│       ├── site.yml
│       ├── deploy-backend.yml
│       ├── deploy-frontend.yml
│       └── install-dependencies.yml
│
└── README.md                     # Este arquivo
```

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📝 Variáveis de Ambiente

### Backend (`application.properties`)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/soulsurf
spring.datasource.username=postgres
spring.datasource.password=senha

# JWT
jwt.secret=seu_secret_key_aqui
jwt.expiration=86400000

# Storage
storage.location=./uploads

# OpenWeather
openweather.api.key=sua_api_key_aqui

# SMTP (email - opcional)
spring.mail.host=smtp.gmail.com
spring.mail.username=seu_email
spring.mail.password=senha_app
```

### Frontend (`.env`)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
```

---

## 🐛 Troubleshooting

### Backend não inicia
- Verificar se PostgreSQL está rodando
- Conferir credenciais em `application.properties`
- Verificar porta 8080 disponível

### Frontend não conecta ao backend
- Verificar `VITE_API_BASE_URL` no `.env`
- Conferir CORS no backend (`WebSecurityConfig`)
- Verificar se backend está rodando

### WebSocket não conecta
- Verificar `VITE_WS_URL` no `.env`
- Conferir token JWT válido
- Verificar logs do backend para erros de handshake

### Upload de arquivos falha
- Verificar permissões da pasta `uploads/`
- Conferir `storage.location` em `application.properties`
- Verificar tamanho máximo de arquivo

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos na disciplina Projeto Aplicado 2.

---

## 🎓 Projeto Acadêmico

**Disciplina**: Projeto Aplicado 2  
**Instituição**: Universidade de Fortaleza (UNIFOR)  
**Período**: 2025

---

<p align="center">
  <strong>🏄 Soul Surf — Conectando surfistas através da tecnologia 🌊</strong>
</p>
