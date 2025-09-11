import React from 'react';
import { Avatar, AvatarFallback, AvatarImage, Button } from './ui/application';
import { BeachCard } from './BeachCard';
import { HashtagCard } from './HashtagCard';

function SideBarRight() {
  return (
    <div className="h-full flex flex-col items-center border border-amber-400">
      <div className="border border pink h-1/2 w-full">
        <BeachCard />
      </div>
      <div className="border border ambar h-1/2 w-full">
        <HashtagCard></HashtagCard>
      </div>
    </div>
  );
}

export default SideBarRight;
