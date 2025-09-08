import React from 'react'
import {Avatar, Button} from '@/components/ui/application'
import SideBar from '@/components/SideBar'
function HomePage() {
  return (
    <div className="flex w-full min-h-screen gap-3">
      <div className="flex gap-5 flex-col hidden md:flex w-[30%] border red-400 justify-center ">
        {/*Coluna da esquerda */}
        <SideBar></SideBar>
      </div>
      <div className="w-full md:w-[40%] border green-400">
        {/*Coluna do meio (feed) */}
      </div>
      <div className="hidden md:flex w-[30%] border  blue-400 ">
        {/*Coluna da direita */}
      </div>
    </div>
  )
}

export default HomePage