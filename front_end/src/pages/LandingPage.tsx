import {
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/application"
import LandingCard from "../components/LandingCard"


function LandingPage() {
  return (
<div className="h-screen w-full flex flex-col">

      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          
        </ResizablePanel>



        <ResizablePanel defaultSize={40}>
          <LandingCard/>
        </ResizablePanel>

  

        <ResizablePanel defaultSize={30}>
          
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  )
}
export default LandingPage