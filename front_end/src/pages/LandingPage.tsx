import LandingCard from "../components/customCards/LandingCard";
import { FaInstagram, FaFacebook, FaTwitter } from "react-icons/fa";
import React from "react";

// --- Subcomponente para o Background de Vídeo ---
// Este componente encapsula a lógica do vídeo de fundo,
// tornando-o reutilizável e mantendo o componente principal mais limpo.
interface VideoBackgroundProps {
  videoSrc: string;
}

const VideoBackground: React.FC<VideoBackgroundProps> = ({ videoSrc }) => (
  // Container com posicionamento absoluto para preencher toda a tela.
  // 'overflow-hidden' garante que o vídeo não ultrapasse os limites do container.
  // '-z-10' coloca o vídeo atrás de todo o conteúdo da página.
  <div className="absolute inset-0 overflow-hidden -z-10">
    <video
      // `autoPlay`: Inicia o vídeo automaticamente.
      // `loop`: Faz o vídeo recomeçar ao terminar.
      // `muted`: Essencial para o autoPlay funcionar na maioria dos navegadores.
      // `playsInline`: Garante que o vídeo rode no próprio local em dispositivos móveis (iOS).
      autoPlay
      loop
      muted
      playsInline
      // `w-full h-full`: O vídeo ocupa todo o espaço do container pai.
      // `object-cover`: Redimensiona o vídeo para cobrir todo o container,
      // cortando as partes excedentes, mas mantendo a proporção.
      className="w-full h-full object-cover"
      src={videoSrc}
    />
    {/*
      Overlay (sobreposição) escuro.
      Este div fica sobre o vídeo para escurecê-lo, garantindo que o texto
      e outros elementos na frente (como o card) tenham contraste suficiente
      para serem legíveis. `bg-black/60` aplica um fundo preto com 60% de opacidade.
    */}
    <div className="absolute inset-0 bg-black/60" />
  </div>
);

// --- Subcomponente para o Cabeçalho ---
// Um cabeçalho simples para dar mais contexto à página.
interface HeaderProps {
  title: string;
  subtitle: string;
}

const Header: React.FC<HeaderProps> = ({ title, subtitle }) => (
  <header className="absolute top-0 left-0 w-full p-6">
    {/*
      `tracking-wider`: Aumenta o espaçamento entre as letras para um visual mais estilizado.
    */}
    <h1 className="text-2xl font-bold text-white tracking-wider">
      {title}
      {/* `font-light`: Deixa a segunda parte do título com uma fonte mais fina, criando um contraste visual. */}
      <span className="font-light">{subtitle}</span>
    </h1>
  </header>
);

// --- Subcomponente para o Conteúdo Principal ---
// Centraliza o LandingCard e aplica as animações de entrada.
const MainContent = () => (
  // `flex-1`: Faz este container ocupar todo o espaço vertical disponível entre o Header e o Footer.
  // `flex items-center justify-center`: Centraliza o conteúdo (LandingCard) tanto vertical quanto horizontalmente.
  // `animate-fade-in-up`: Aplica a animação customizada definida no tailwind.config.js.
  <main className="flex-1 flex items-center justify-center animate-fade-in-up">
    <LandingCard />
  </main>
);

// --- Dados para os links de redes sociais ---
// Estruturar os dados assim torna o componente Footer mais limpo e fácil de manter.
const socialLinks = [
  {
    name: "Instagram",
    href: "https://instagram.com",
    icon: FaInstagram,
  },
  {
    name: "Facebook",
    href: "https://facebook.com",
    icon: FaFacebook,
  },
  {
    name: "Twitter",
    href: "https://twitter.com",
    icon: FaTwitter,
  },
];

// --- Subcomponente para um único link de rede social ---
interface SocialLinkProps {
  href: string;
  ariaLabel: string;
  IconComponent: React.ElementType;
}

const SocialLink: React.FC<SocialLinkProps> = ({ href, ariaLabel, IconComponent }) => (
  <a
    href={href}
    target="_blank" // Abre o link em uma nova aba.
    // `rel="noopener noreferrer"`: Boa prática de segurança para links `target="_blank"`.
    rel="noopener noreferrer"
    aria-label={ariaLabel} // Essencial para acessibilidade, descreve o link para leitores de tela.
    // `text-white/70`: Cor branca com 70% de opacidade.
    // `hover:text-white`: Ao passar o mouse, a opacidade vai para 100%.
    // `transition-colors`: Anima a mudança de cor suavemente.
    className="text-white/70 hover:text-white transition-colors"
  >
    <IconComponent size={24} />
  </a>
);

// --- Subcomponente para o Rodapé ---
// Adiciona links de redes sociais, tornando a página mais completa.
const Footer = () => (
  <footer className="w-full p-6 flex justify-center items-center">
    {/* `gap-6`: Adiciona um espaçamento de 1.5rem (24px) entre cada item do flex container. */}
    <div className="flex gap-6">
      {socialLinks.map((link) => (
        <SocialLink
          key={link.name}
          href={link.href}
          ariaLabel={`Siga-nos no ${link.name}`}
          IconComponent={link.icon}
        />
      ))}
    </div>
  </footer>
);

/**
 * LandingPage
 *
 * A página de entrada principal da aplicação "Soul Surf".
 *
 * ## Visão Geral
 * Esta página serve como o primeiro ponto de contato com o usuário não autenticado.
 * O design foi pensado para ser imersivo e impactante, utilizando um vídeo de fundo
 * relacionado ao surf para estabelecer imediatamente a identidade visual da marca.
 *
 * A estrutura foi refeita sem as dependências do `shadcn/ui` para o layout,
 * utilizando componentes internos e TailwindCSS para maior controle e performance.
 * O layout é construído com Flexbox, garantindo responsividade e alinhamento
 * consistentes em diferentes tamanhos de tela.
 *
 * ## Estrutura de Componentes
 * A página é dividida em subcomponentes lógicos para melhor organização e manutenibilidade:
 *
 * - `VideoBackground`: Responsável por renderizar o vídeo em loop no plano de fundo e
 *   a sobreposição escura que melhora a legibilidade do conteúdo.
 * - `Header`: Exibe o logotipo ou nome da aplicação no canto superior.
 * - `MainContent`: O coração da página, responsável por centralizar o `LandingCard`,
 *   que contém as principais chamadas para ação (login/cadastro).
 * - `Footer`: Um rodapé simples que contém links para as redes sociais da marca,
 *   composto por múltiplos componentes `SocialLink`.
 */
function LandingPage() {
  // A estrutura agora é um container flexível que ocupa toda a tela.
  // A posição `relative` é necessária para o posicionamento absoluto do vídeo.
  return (
    <div className="relative h-screen w-full flex flex-col bg-background text-foreground">
      {/* Componente de vídeo de fundo */}
      <VideoBackground videoSrc="https://cdn.pixabay.com/video/2024/05/20/211530_large.mp4" />

      {/* Componente de cabeçalho */}
      <Header title="Soul" subtitle="Surf" />

      {/* Conteúdo principal que se expande para preencher o espaço */}
      <MainContent />

      {/* Componente de rodapé */}
      <Footer />
    </div>
  );
}

export default LandingPage;