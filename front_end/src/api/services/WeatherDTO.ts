mport axios from "../axios";

export interface WeatherDTO {
    cityName: string;
    temp: number;
    description: string;
    iconCode: string;
}

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export class WeatherService {

    /**
     * Busca o clima atual chamando o endpoint do seu backend Spring Boot.
     * @param city O nome da cidade (Ex: "Fortaleza,BR").
     * @returns Promise<WeatherDTO>
     */
    static async getCurrentWeather(city: string = "Fortaleza,BR"): Promise<WeatherDTO> {

        const endpoint = `${API_URL}/weather/current`;

        try {
            const response = await axios.get<WeatherDTO>(endpoint, {
                params: { city: city } // Passa a cidade como query parameter
            });

            // Retorna os dados mapeados para a interface WeatherDTO
            return response.data;

        } catch (error) {
            console.error(`[WeatherService] Erro ao buscar dados de clima para ${city}:`, error);

            // Retorna um objeto de dados de fallback em caso de falha da API
            return {
                cityName: "API OFFLINE",
                temp: 0,
                description: "Dados indisponíveis",
                iconCode: "50d",
            } as WeatherDTO;
        }
    }
}