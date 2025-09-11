import surf01 from "../assets/login_page/surf01.jpg"
import surf02 from "../assets/login_page/surf02.jpg"

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/application"
import ForgotPasswordCard from "../components/ForgotPasswordCard"

function ForgotPasswordPage() {
  return (
    <div className="h-screen w-full flex flex-col bg-background">
      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
            <img src={surf01} alt="Foto Surf 1" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={40}>
          {/* Aqui chamamos o card que já temos pronto */}
          <ForgotPasswordCard />
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
            <img src={surf02} alt="Foto Surf 2" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}
export default ForgotPasswordPage;

