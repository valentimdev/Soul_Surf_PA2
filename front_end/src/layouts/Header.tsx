import { Bell, CloudRain, Wind, Waves, Moon, Search, Thermometer, Cloud } from "lucide-react";
import { NotificationService, type NotificationDTO } from "@/api/services/notificationService";
import { useEffect, useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import soulSurfIcon from "../assets/header/SoulSurfIcon.png";
import { Input } from "@/components/ui/input";
import { UserService, type UserDTO } from "@/api/services/userService";
import { WeatherService, type WeatherDTO } from "@/api/services/WeatherService";
import { useLocation } from "react-router-dom";

function Header() {
    const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
    const [weatherData, setWeatherData] = useState<WeatherDTO | null>(null);
    const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const location = useLocation();
    const isUserTimeline = location.pathname === "/usertimeline";

    const surfConditions = {
        vento: "12 km/h NE",
        ondas: "1.8 m",
        lua: "🌔 ",
        mare: "0.9 m",
    };

    useEffect(() => {
        const fetchUserAndWeather = async () => {
            try {
                const user = await UserService.getMe();
                setCurrentUser(user);
                const weather = await WeatherService.getCurrentWeather("Fortaleza,BR");
                setWeatherData(weather);
            } catch (error) {}
        };
        fetchUserAndWeather();
    }, []);

    useEffect(() => {
        const interval = setInterval(async () => {
            try {
                const notifs = await NotificationService.getMyNotifications();
                setNotifications(notifs);
            } catch {}
        }, 30000);
        return () => clearInterval(interval);
    }, []);

    const handleNotificationClick = async (notif: NotificationDTO) => {
        try {
            if (!notif.read) {
                await NotificationService.markAsRead(notif.id);
                setNotifications((prev) =>
                    prev.map((n) => (n.id === notif.id ? { ...n, read: true } : n))
                );
            }
            if (notif.postId) {
                window.location.href = `/posts/${notif.postId}`;
            }
        } catch {}
    };

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            const target = e.target as HTMLElement;
            if (target.closest(".notification-dropdown") || target.closest(".notification-button")) return;
            setShowDropdown(false);
        };
        document.addEventListener("click", handleClickOutside);
        return () => document.removeEventListener("click", handleClickOutside);
    }, []);

    const cityName = weatherData ? weatherData.cityName.split(",")[0] : "Local";
    const formattedTemp = weatherData ? `${Math.round(weatherData.temp)}°C` : "--°C";
    const weatherDescription = weatherData ? weatherData.description : "Clima Desconhecido";

    const WeatherIcon = () => {
        if (!weatherData) return <Cloud size={12} />;
        if (weatherData.description.toLowerCase().includes("chuva")) return <CloudRain size={12} />;
        if (weatherData.iconCode.includes("01d")) return <span className="text-xl">☀️</span>;
        return <Cloud size={12} />;
    };

    const unreadCount = notifications.filter((n) => !n.read).length;

    return (
        <header className="bg-[var(--primary)] h-20 w-full flex items-center justify-center md:justify-between px-6 relative z-100">
            <div className="flex items-center h-full">
                <img
                    src={soulSurfIcon}
                    alt="Soul Surf Logo"
                    className="h-full w-auto cursor-pointer"
                    onClick={() => (window.location.href = "/home")}
                />
            </div>

            <div className="hidden md:block absolute w-1/3 left-1/2 -translate-x-1/2">
                <div className="relative w-full">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                    <Input
                        type="text"
                        placeholder={
                            isUserTimeline
                                ? "Buscar surfistas..."
                                : "Buscar praias, surfistas, comunidades..."
                        }
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                if (isUserTimeline) {
                                    window.dispatchEvent(
                                        new CustomEvent("searchUsers", { detail: searchQuery })
                                    );
                                } else {
                                    window.dispatchEvent(
                                        new CustomEvent("searchGeneric", { detail: searchQuery })
                                    );
                                }
                            }
                        }}
                        className="pl-10 pr-4 py-2 rounded-full border border-gray-300 bg-white focus:ring-2 focus:ring-blue-400"
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
                        <div className="flex items-center gap-1">🌊 {surfConditions.mare}</div>
                    </div>
                </div>

                <div className="relative">
                    <button
                        onClick={() => setShowDropdown((prev) => !prev)}
                        className="relative text-white hover:text-yellow-300 transition notification-button"
                    >
                        <Bell size={26} />
                        {unreadCount > 0 && (
                            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-4 h-4 rounded-full flex items-center justify-center">
                                {unreadCount}
                            </span>
                        )}
                    </button>

                    {showDropdown && (
                        <div className="absolute right-0 mt-3 w-80 bg-white shadow-2xl rounded-xl overflow-hidden z-50 notification-dropdown border">
                            <div className="px-4 py-3 border-b font-semibold text-gray-700 bg-gray-50">
                                Notificações
                            </div>
                            <div className="max-h-96 overflow-y-auto">
                                {notifications.length === 0 ? (
                                    <div className="p-4 text-gray-500 text-center">Nenhuma notificação</div>
                                ) : (
                                    notifications.map((n) => (
                                        <div
                                            key={n.id}
                                            className={`px-4 py-3 border-b cursor-pointer hover:bg-gray-50 transition ${
                                                !n.read ? "bg-blue-50" : ""
                                            }`}
                                            onClick={() => handleNotificationClick(n)}
                                        >
                                            <p className="text-sm text-gray-800">{n.message}</p>
                                            <p className="text-xs text-gray-400">
                                                {new Date(n.createdAt).toLocaleString()}
                                            </p>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    )}
                </div>

                <Avatar
                    className="w-11 h-11 cursor-pointer"
                    onClick={() => (window.location.href = "/perfil")}
                >
                    {currentUser?.fotoPerfil ? (
                        <AvatarImage
                            className="rounded-full border border-white"
                            src={currentUser.fotoPerfil}
                            alt={currentUser.username}
                        />
                    ) : (
                        <AvatarFallback>
                            {currentUser?.username ? currentUser.username.charAt(0) : "?"}
                        </AvatarFallback>
                    )}
                </Avatar>
            </div>
        </header>
    );
}

export default Header;
