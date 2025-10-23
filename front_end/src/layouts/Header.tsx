import { useEffect, useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import soulSurfIcon from "../assets/header/SoulSurfIcon.png";
import { CloudRain, Wind, Waves, Moon, Search, Thermometer, Cloud } from "lucide-react";
import { Input } from "@/components/ui/input";
import { UserService, type UserDTO } from "@/api/services/userService";
import { WeatherService, type WeatherDTO } from "@/api/services/WeatherService";
function Header() {
    const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
    const [weatherData, setWeatherData] = useState<WeatherDTO | null>(null); // Estado para dados da API de clima

    // Dados de Surf MOCK/Estáticos (Ainda não vêm de uma API)
    const surfConditions = {
        vento: "12 km/h NE",
        ondas: "1.8 m",
        lua: "🌔 ",
        mare: "0.9 m",
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Carregar Usuário
                const user = await UserService.getMe();
                setCurrentUser(user);

                // 2. Carregar Clima (Altere a cidade padrão para a sua preferência)
                const weather = await WeatherService.getCurrentWeather("Fortaleza,BR");
                setWeatherData(weather);

            } catch (error) {
                console.error("Erro ao carregar dados do Header:", error);
                // Continua com null/dados estáticos se a API falhar
            }
        };

        fetchData();
    }, []);

    // Formatação dos dados de clima para exibição
    const cityName = weatherData ? weatherData.cityName.split(',')[0] : "Local";
    const formattedTemp = weatherData ? `${Math.round(weatherData.temp)}°C` : "--°C";
    const weatherDescription = weatherData ? weatherData.description : "Clima Desconhecido";

    // Simples função para selecionar o ícone de clima com base na descrição (ou usar o iconCode)
    const WeatherIcon = () => {
        if (!weatherData) return <Cloud size={12} />;
        // Exemplo: se a descrição incluir "chuva"
        if (weatherData.description.toLowerCase().includes("chuva")) {
            return <CloudRain size={12} />;
        }
        // Exemplo: se o código do ícone for de dia e limpo (você pode mapear melhor)
        if (weatherData.iconCode.includes("01d")) {
            // Se for sol
            return <span className="text-xl leading-none">☀️</span>;
        }
        return <Cloud size={12} />; // Padrão
    };
    return (
        <header className="bg-[var(--primary)] h-20 w-full flex items-center justify-between px-6 relative z-100">
            <div className="flex items-center h-full">
                <img
                    src={soulSurfIcon}
                    alt="Soul Surf Logo"
                    className="h-full w-auto cursor-pointer"
                    onClick={() => (window.location.href = "/home")}
                />
            </div>

            <div className="absolute w-1/3 left-1/2 -translate-x-1/2">
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

           <div className="flex items-center gap-8">
                <div className="flex flex-col text-xs text-white font-medium text-center">

                    {/* LINHA 1: CLIMA E TEMPERATURA (DADOS REAIS DA API) */}
                    <div className="flex flex-row gap-2 justify-center text-sm font-bold mb-1">

                        {/* 🌡️ Cidade e Temperatura */}
                        <div className="flex items-center gap-1">
                            <Thermometer size={14} />
                            {cityName} {formattedTemp}
                        </div>

                        {/* ☁️ Descrição do Clima */}
                        <div className="flex items-center gap-1">
                            <WeatherIcon /> {weatherDescription}
                        </div>
                    </div>

                    {/* LINHA 2: CONDIÇÕES DE SURF (DADOS MOCK) */}
                    <div className="flex flex-row gap-2 justify-center">

                        {/* Vento (Poderia vir da API se tivesse a propriedade) */}
                        <div className="flex items-center gap-1">
                            <Wind size={12} /> {surfConditions.vento}
                        </div>

                        {/* Ondas */}
                        <div className="flex items-center gap-1">
                            <Waves size={12} /> {surfConditions.ondas}
                        </div>

                        {/* Lua */}
                        <div className="flex items-center gap-1">
                            <Moon size={12} /> {surfConditions.lua}
                        </div>

                        {/* Maré */}
                        <div className="flex items-center gap-1">🌊 {surfConditions.mare}</div>
                    </div>
                </div>

                {/* Avatar dinâmico */}
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
