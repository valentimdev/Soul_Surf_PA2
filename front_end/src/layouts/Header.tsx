import {
  CloudRain,
  Wind,
  Waves,
  Moon,
  Search,
  Thermometer,
  Cloud,
  Menu,
  X,
  Home,
  User,
  Users,
  MessageSquare,
  Settings,
  LogOut,
} from 'lucide-react';
import NotificationDropdown from '@/components/NotificationDropdown';
import { useEffect, useState } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import soulSurfIcon from '../assets/header/SoulSurfIcon.png';
import { Input } from '@/components/ui/input';
import { UserService, type UserDTO } from '@/api/services/userService';
import { WeatherService, type WeatherDTO } from '@/api/services/WeatherService';
import { useLocation, Link, useNavigate } from 'react-router-dom';

function Header() {
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [weatherData, setWeatherData] = useState<WeatherDTO | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();
  const isUserTimeline = location.pathname === '/usertimeline';
  const navigate = useNavigate();

  const surfConditions = {
    vento: '12 km/h NE',
    ondas: '1.8 m',
    lua: '🌔 ',
    mare: '0.9 m',
  };

  useEffect(() => {
    const fetchUserAndWeather = async () => {
      try {
        const user = await UserService.getMe();
        setCurrentUser(user);
        const weather = await WeatherService.getCurrentWeather('Fortaleza,BR');
        setWeatherData(weather);
      } catch (error) { }
    };
    fetchUserAndWeather();
  }, []);

  // Fechar menu mobile ao mudar de página
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  // Prevenir scroll quando o menu mobile estiver aberto
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isMobileMenuOpen]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  const menuItems = [
    { href: '/home', icon: Home, label: 'Início' },
    { href: '/perfil', icon: User, label: 'Perfil' },
    { href: '/praias', icon: Waves, label: 'Praias' },
    { href: '/usuarios', icon: Users, label: 'Usuários' },
    { href: '/mensagens', icon: MessageSquare, label: 'Mensagens' },
  ];

  const cityName = weatherData ? weatherData.cityName.split(',')[0] : 'Local';
  const formattedTemp = weatherData
    ? `${Math.round(weatherData.temp)}°C`
    : '--°C';
  const weatherDescription = weatherData
    ? weatherData.description
    : 'Clima Desconhecido';

  const WeatherIcon = () => {
    if (!weatherData) return <Cloud size={12} />;
    if (weatherData.description.toLowerCase().includes('chuva'))
      return <CloudRain size={12} />;
    if (weatherData.iconCode.includes('01d'))
      return <span className="text-xl">☀️</span>;
    return <Cloud size={12} />;
  };

  return (
    <>
      <header className="bg-[var(--primary)] h-20 w-full flex items-center justify-between md:justify-between px-6 relative z-100">
        <button
          onClick={() => setIsMobileMenuOpen(true)}
          className="md:hidden  p-2 text-white hover:bg-white/10 rounded-md transition-colors"
          aria-label="Abrir menu"
        >
          <Menu size={24} />
        </button>


        <div className="absolute left-1/2 -translate-x-1/2 md:relative md:left-0 md:translate-x-0 h-full flex items-center">
          <img
            src={soulSurfIcon}
            alt="Soul Surf Logo"
            className="ml-12 h-full w-auto cursor-pointer"
            onClick={() => (window.location.href = '/home')}
          />
        </div>

        <div className="hidden md:block absolute w-1/3 left-1/2 -translate-x-1/2">
          <div className="relative w-full">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
              size={18}
            />
            <Input
              type="text"
              placeholder={
                isUserTimeline
                  ? 'Buscar surfistas...'
                  : 'Buscar praias, surfistas, comunidades...'
              }
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  navigate(`/buscar?query=${encodeURIComponent(searchQuery)}`);
                }
              }}
              className="pl-10 pr-4 py-2 rounded-full border border-gray-300 bg-white focus:ring-2 "
            />
          </div>
        </div>

        <div className="hidden md:flex items-center gap-8">
          <div className="flex flex-col text-xs text-white font-medium text-center">
            <div className="flex flex-row gap-2 justify-center text-sm font-bold mb-1">
              <div className="flex items-center gap-1">
                <Thermometer size={14} />
                {cityName} {formattedTemp}
              </div>
              <div className="flex items-center gap-1">
                <WeatherIcon /> {weatherDescription}
              </div>
            </div>

            <div className="flex flex-row gap-2 justify-center">
              <div className="flex items-center gap-1">
                <Wind size={12} /> {surfConditions.vento}
              </div>
              <div className="flex items-center gap-1">
                <Waves size={12} /> {surfConditions.ondas}
              </div>
              <div className="flex items-center gap-1">
                <Moon size={12} /> {surfConditions.lua}
              </div>
              <div className="flex items-center gap-1">
                🌊 {surfConditions.mare}
              </div>
            </div>
          </div>

          {/* Dropdown de Notificações */}
          <NotificationDropdown />

          <Avatar
            className="w-11 h-11 cursor-pointer hover:ring-2 hover:ring-yellow-300 transition-all"
            onClick={() => (window.location.href = '/perfil')}
          >
            {currentUser?.fotoPerfil ? (
              <AvatarImage
                className="rounded-full border border-white"
                src={currentUser.fotoPerfil}
                alt={currentUser.username}
              />
            ) : (
              <AvatarFallback>
                {currentUser?.username ? currentUser.username.charAt(0) : '?'}
              </AvatarFallback>
            )}
          </Avatar>
        </div>
      </header>

      {/* Menu Mobile Slide */}
      <div
        className={`fixed inset-0 z-[9999] md:hidden transition-all duration-300 ${isMobileMenuOpen ? 'visible opacity-100' : 'invisible opacity-0'
          }`}
      >
        {/* Overlay */}
        <div
          className={`absolute inset-0 bg-black/50 transition-opacity duration-300 ${isMobileMenuOpen ? 'opacity-100' : 'opacity-0'
            }`}
          onClick={() => setIsMobileMenuOpen(false)}
        />

        {/* Menu Slide */}
        <div
          className={`absolute left-0 top-0 h-full w-80 max-w-[85vw] bg-white shadow-xl transform transition-transform duration-300 ease-out ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
            }`}
        >
          {/* Header do menu */}
          <div className="flex items-center justify-between p-4 border-b bg-[var(--primary)]">
            <h2 className="text-white font-semibold text-lg">Menu</h2>
            <button
              onClick={() => setIsMobileMenuOpen(false)}
              className="p-2 text-white hover:bg-white/10 rounded-md transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          {/* Perfil no menu */}
          <div className="p-4 border-b bg-gray-50">
            <div className="flex items-center gap-3">
              <Avatar className="w-12 h-12">
                {currentUser?.fotoPerfil ? (
                  <AvatarImage
                    src={currentUser.fotoPerfil}
                    alt={currentUser.username}
                    className="rounded-full"
                  />
                ) : (
                  <AvatarFallback>
                    {currentUser?.username
                      ? currentUser.username.charAt(0)
                      : '?'}
                  </AvatarFallback>
                )}
              </Avatar>
              <div>
                <p className="font-medium text-gray-900">
                  {currentUser?.username || 'Usuário'}
                </p>
                <p className="text-sm text-gray-500">
                  {currentUser?.email || 'email@exemplo.com'}
                </p>
              </div>
            </div>
          </div>

          {/* Itens do menu */}
          <nav className="flex-1 p-4">
            <ul className="space-y-1">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.href;
                return (
                  <li key={item.label}>
                    <Link
                      to={item.href}
                      className={`flex items-center gap-3 p-3 rounded-lg transition-colors ${isActive
                        ? 'bg-[#eae8dc] text-primary font-medium'
                        : 'text-gray-700 hover:bg-gray-50'
                        }`}
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <item.icon size={20} />
                      <span className="flex-1">{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>

            {/* Separador */}
            <hr className="my-4 border-gray-200" />

            {/* Configurações */}
            <button
              className="flex items-center gap-3 p-3 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors w-full"
              onClick={() => {
                alert('Configurações em desenvolvimento...');
                setIsMobileMenuOpen(false);
              }}
            >
              <Settings size={20} />
              Configurações
            </button>

            {/* Logout */}
            <button
              onClick={() => {
                handleLogout();
                setIsMobileMenuOpen(false);
              }}
              className="flex items-center gap-3 p-3 rounded-lg text-red-600 hover:bg-red-50 transition-colors w-full"
            >
              <LogOut size={20} />
              Sair
            </button>
          </nav>
        </div>
      </div >
    </>
  );
}

export default Header;
