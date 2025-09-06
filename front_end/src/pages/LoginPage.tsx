import surf01 from "../assets/login_page/surf01.jpg"
import surf02 from "../assets/login_page/surf02.jpg"

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/application"
import LoginCard from "../components/LoginCard"

function LoginPage() {
  return (
<div className="h-screen w-full flex flex-col">
      <header className="p-6 text-xl font-semibold">
      </header>

      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
              <img src={surf01} alt="Foto Surf 1" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={40}>
          <LoginCard/>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
              <img src={surf02} alt="Foto Surf 2" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  )
}
export default LoginPage