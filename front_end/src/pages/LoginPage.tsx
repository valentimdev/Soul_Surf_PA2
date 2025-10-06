import surf01 from "../assets/login_page/surf01.jpg";
// Não precisamos mais de ResizablePanelGroup e Handle
// import {
//   ResizableHandle,
//   ResizablePanel,
//   ResizablePanelGroup,
// } from "@/components/ui/resizable";
import LoginCard from "../components/customCards/LoginCard";

function LoginPage() {
  return (
    // Div principal que ocupa todo o ecrã
    <div
      className="h-screen w-full flex items-center justify-center bg-cover bg-center"
      style={{ backgroundImage: `url(${surf01})` }} // Define a imagem de fundo aqui
    >
      {/* O LoginCard agora está diretamente centrado */}
      <LoginCard />
    </div>
  );
}
export default LoginPage;