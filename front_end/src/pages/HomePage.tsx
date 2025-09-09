import React from 'react';
import { Avatar, Button } from '@/components/ui/application';
import { PostCard } from '@/components/PostCard';
import SideBar from '@/components/SideBar';
function HomePage() {
  return (
    <div className="flex w-full min-h-screen gap-3">
      <div className="hidden md:block w-[20%]">
        {/*Coluna da esquerda */}
        <div className="fixed w-[20%] h-full">
          <SideBar />
        </div>
      </div>
      <div className="w-full md:w-[60%] border-green-400 py-4 space-y-4">
        {/*Coluna do meio (feed) */}
        <PostCard />
        <PostCard />
        <PostCard />
      </div>
      <div className="hidden md:block w-[20%]">
        <div className="fixed w-[20%] h-full border blue-400 ">
          {/*Coluna da direita */}
          <h1>Placeholder</h1>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
