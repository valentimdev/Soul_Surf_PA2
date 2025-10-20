import api from "../axios";
import { mencoesRoutes } from "../routes/mencoes";
import type { UserDTO } from "./userService";

export const MencoesService = {
    getSuggestions: async (query: string): Promise<UserDTO[]> => {
        const { data } = await api.get<UserDTO[]>(mencoesRoutes.mentionSuggestions(), {
            params: { query },
        });
        return data;
    },
};