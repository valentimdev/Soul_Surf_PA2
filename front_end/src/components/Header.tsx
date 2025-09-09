import { Avatar, AvatarFallback, AvatarImage } from "@radix-ui/react-avatar";
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
          onClick={() => { window.location.href = '/' }}
          style={{ cursor: 'pointer' }}
        />
      </div>
      <div className="flex items-center h-full">
        <Avatar className="w-11 h-11">
          <AvatarImage
            className="rounded-full border border white"
            src="https://img.olympics.com/images/image/private/t_1-1_300/f_auto/primary/rousxeo5xvuqj3qvrxzq"
          />
          <AvatarFallback>CN</AvatarFallback>
        </Avatar>
      </div>

    </header>
  )
}

export default Header