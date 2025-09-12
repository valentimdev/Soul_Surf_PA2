import CadastroCard from '@/components/customCards/CadastroCard';
import surf01 from '../assets/login_page/surf01.jpg';
import surf02 from '../assets/login_page/surf02.jpg';

import {
  ResizablePanel,
  ResizablePanelGroup,
} from '@/components/ui/resizable';

function CadastroPage() {
  return (
    <div className="h-screen w-full flex flex-col">
      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
            <img
              src={surf01}
              alt="Foto Surf 1"
              className="w-full h-full object-cover"
            />
          </div>
        </ResizablePanel>

        <ResizablePanel defaultSize={40}>
          <div className="h-full flex items-center justify-center">
            <CadastroCard></CadastroCard>
          </div>
        </ResizablePanel>

        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
            <img
              src={surf02}
              alt="Foto Surf 2"
              className="w-full h-full object-cover"
            />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}

export default CadastroPage;
