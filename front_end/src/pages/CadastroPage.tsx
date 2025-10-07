import CadastroCard from '@/components/customCards/CadastroCard';
import surfimagetest from '../assets/surfimagetest.jpg';
// Não precisamos mais de ResizablePanelGroup e Handle
// import {
//   ResizablePanel,
//   ResizablePanelGroup,
// } from '@/components/ui/resizable';

function CadastroPage() {
  return (
    // Div principal que ocupa todo o ecrã
    <div
      className="h-screen w-full flex items-center justify-center bg-cover bg-center"
      style={{ backgroundImage: `url(${surfimagetest})` }} // Define a imagem de fundo aqui
    >
      {/* O CadastroCard agora está diretamente centrado */}
      <CadastroCard />
    </div>
  );
}

export default CadastroPage;