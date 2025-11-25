 import surfzada from "../assets/surfzada.jpg";
 import surf01 from "../assets/login_page/surf01.jpg"

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/resizable"
import ForgotPasswordCard from "../components/customCards/ForgotPasswordCard"
function ForgotPasswordPage() {
  return (
    <div className="h-screen w-full flex flex-col bg-background">
      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
              <img src={surf01} alt="Foto Surf 2" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={40}>
          <ForgotPasswordCard />
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
            <img src={surfzada} alt="Foto Surf 2" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}
export default ForgotPasswordPage;

