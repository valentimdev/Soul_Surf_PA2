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
      <div className="hidden md:block w-[20%]"></div>
      <div className="w-full md:w-[60%] border-green-400 py-4 space-y-4">
        {/*Coluna do meio (feed) */}
        <PostCard
          username="Thiago Surfista"
          userAvatarUrl=""
          imageUrl="src/assets/soul_surfer_2.jpeg"
          description="Tava paia o mar mas fiz acontecer"
        />
          <PostCard
            username="Gabriel Medina"
            userAvatarUrl="https://img.olympics.com/images/image/private/t_1-1_300/f_auto/v1707814715/primary/rfokftspfqn6yomtoisa"
            imageUrl="https://img.olympics.com/images/image/private//t_s_w960/f_auto/primary/ckfbhcgmmpqmx9pxdkfl"
            description="Mãe, consegui a medalha bronze!."
          />
        <PostCard
          username="Thiago Surfista"
          userAvatarUrl=""
          imageUrl="src/assets/soul_surfer_1.jpeg"
          description="Hoje prestou aleluia."
        />
      </div>
    </div>
  );
}

export default HomePage;
