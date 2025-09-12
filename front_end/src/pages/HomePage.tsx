import React from 'react';
import { Avatar, Button } from '@/components/ui/application';
import { PostCard } from '@/components/customCards/PostCard';
import SideBarLeft from '@/layouts/SideBarLeft';
import { BeachCard } from '@/components/customCards/BeachCard';
import { HashtagCard } from '@/components/customCards/HashtagCard';
import SideBarRight from '@/layouts/SideBarRight';
function HomePage() {
  return (
    <div className="flex w-full min-h-screen gap-3">
      <div className="hidden md:block w-[20%]">
        {/*Coluna da esquerda */}
        <div className="fixed w-[20%] h-full">
          <SideBarLeft />
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
          <SideBarRight></SideBarRight>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
