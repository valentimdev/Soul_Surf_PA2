import React from 'react';
import { Avatar, Button } from './ui/application';

function SideBar() {
  return (
    <div className="flex flex-col">
      {' '}
      <Avatar></Avatar>
      <Button variant="outline" className="width-40%">
        Home
      </Button>
      <Button variant="outline" className="width-40%">
        Seguindo
      </Button>
      <Button variant="outline" className="width-40%">
        Home
      </Button>
      <Button variant="outline" className="width-40%">
        Home
      </Button>
      <Button variant="outline" className="width-40%">
        Home
      </Button>
    </div>
  );
}

export default SideBar;
