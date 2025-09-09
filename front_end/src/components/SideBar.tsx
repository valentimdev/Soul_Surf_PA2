import React from 'react';
import { Avatar, AvatarFallback, AvatarImage, Button } from './ui/application';

function SideBar() {
  return (
    <div className="h-full flex flex-col gap-5 items-center border border-amber-400">
      <div className="border border-purple-400 w-full h-[30%] flex justify-center items-center mt-3">
        <div className="flex flex-col items-center gap-4">

          <a href="#" className="text-sm">Kelly Slater</a>
        </div>
      </div>
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

export default SideBar;
