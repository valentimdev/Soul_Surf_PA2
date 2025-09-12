import React from 'react';
import { Button } from '../components/ui/button';

function SideBarLeft() {
  return (
    <div className="justify-center h-full flex flex-col gap-5 items-center border border-amber-400">
      <Button variant="outline" className="text-sm px-2 py-1 w-60">
        Home
      </Button>
      <Button variant="outline" className=" text-sm px-2 py-1 w-60">
        Seguindo
      </Button>
      <Button variant="outline" className=" text-sm px-2 py-1 w-60">
        Praias
      </Button>
      <Button variant="outline" className=" text-sm px-2 py-1 w-60">
        Comunidades
      </Button>
      <Button variant="outline" className=" text-sm px-2 py-1 w-60">
        Configurações
      </Button>
    </div>
  );
}

export default SideBarLeft;
