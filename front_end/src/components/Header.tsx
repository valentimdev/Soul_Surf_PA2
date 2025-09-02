import React from 'react'
import { CgProfile } from "react-icons/cg";

function Header() {
  return (
    <header className="bg-[var(--primary)] p-6 w-full flex flex-col">
      <h1 className="text-white  text-2xl ">Soul Surf</h1>
      <div className="absolute top-6 right-6">
        <CgProfile className="text-white text-2xl h-8 w-8" />
      </div>
    </header>
  )
}

export default Header