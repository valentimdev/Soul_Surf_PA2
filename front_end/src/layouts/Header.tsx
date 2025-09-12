import { Avatar, AvatarFallback, AvatarImage } from '@radix-ui/react-avatar';
import soulSurfIcon from '../assets/header/SoulSurfIcon.png';
import { CloudRain, Wind, Waves, Moon, Search } from 'lucide-react';
import { Input } from '@/components/ui/input';

function Header() {
  // Dados mockados
  const surfConditions = {
    vento: '12 km/h NE',
    chuva: '10%',
    ondas: '1.8 m',
    lua: '🌔 ',
    mare: '0.9 m',
  };

  return (
    <header className="bg-[var(--primary)] h-20 w-full flex items-center justify-between px-6 relative">
      {/* Logo */}
      <div className="flex items-center h-full">
        <img
          src={soulSurfIcon}
          alt="Soul Surf Logo"
          className="h-full w-auto"
          onClick={() => {
            window.location.href = '/';
          }}
          style={{ cursor: 'pointer' }}
        />
      </div>

      {/* Centro: Search bar */}
      <div className="absolute w-1/2 left-1/2 -translate-x-1/2">
        <div className="relative w-full">
          <Search
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            size={18}
          />
          <Input
            type="text"
            placeholder="Buscar praias, surfistas, comunidades..."
            className="pl-10 pr-4 py-2 rounded-full border border-gray-300 bg-white focus:ring-2 focus:ring-blue-400"
          />
        </div>
      </div>

      {/* Direita: Condições + Avatar */}
      <div className="flex items-center gap-8">
        {/* Condições do mar */}
        <div className=" flex flex-col text-xs text-white font-medium justify-center items-center ">
          <div className="flex flex-row gap-2">
            <div className="flex items-center gap-">
              <Wind size={12} /> {surfConditions.vento}
            </div>
            <div className="flex items-center gap-1">
              <CloudRain size={12} /> {surfConditions.chuva}
            </div>
            <div className="flex items-center gap-1">
              <Waves size={12} /> {surfConditions.ondas}
            </div>
          </div>
          <div className="flex flex-row gap-2">
            <div className="flex items-center gap-1 col-start-1">
              <Moon size={12} /> {surfConditions.lua}
            </div>
            <div className="flex items-center gap-1">
              🌊 {surfConditions.mare}
            </div>
          </div>
        </div>

        {/* Avatar no canto */}
        <Avatar className="w-11 h-11">
          <AvatarImage
            className="rounded-full border border-white"
            src="https://img.olympics.com/images/image/private/t_1-1_300/f_auto/primary/rousxeo5xvuqj3qvrxzq"
            onClick={() => {
              window.location.href = '/perfil';
            }}
            style={{ cursor: 'pointer' }}
          />
          <AvatarFallback>CN</AvatarFallback>
        </Avatar>
      </div>
    </header>
  );
}

export default Header;
