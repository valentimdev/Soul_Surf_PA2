import soulSurfIcon from "../assets/header/SoulSurfIcon.png"
import { CgProfile } from "react-icons/cg";

function Header() {
  return (
    <header className="bg-[var(--primary)] h-20 w-full flex items-center justify-between px-6">
      <div className="flex items-center h-full">
        <img
          src={soulSurfIcon}
          alt="Soul Surf Logo"
          className="h-full w-auto"
        />
      </div>

      <div className="flex items-center h-full">
        <CgProfile className="text-white text-2xl h-8 w-8"/>
      </div>
    </header>
  )
}

export default Header