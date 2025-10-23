import api from "../axios";
import { beachRoutes } from "../routes/beach";

// --- DTOs do backend ---
export type BeachDTO = {
    id: number;
    nome: string;
    descricao: string;
    localizacao: string;
    caminhoFoto: string;
    nivelExperiencia: string;
};

export type PostDTO = {
    id: number;
    titulo: string;
    descricao: string;
    caminhoFoto?: string;
    data: string;
    usuario: {
        id: number;
        username: string;
        email: string;
        fotoPerfil?: string;
    };
};

export type MessageResponse = {
    message: string;
};

// --- Service ---
export const BeachService = {
    getAllBeaches: async (): Promise<BeachDTO[]> => {
        const { data } = await api.get<BeachDTO[]>(beachRoutes.getAll());
        return data;
    },

    getBeachById: async (id: number | string): Promise<BeachDTO> => {
        const { data } = await api.get<BeachDTO>(beachRoutes.getById(id));
        return data;
    },

    createBeach: async (
        formData: FormData
    ): Promise<MessageResponse> => {
        const { data } = await api.post<MessageResponse>(
            beachRoutes.create(),
            formData,
            {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                    "Content-Type": "multipart/form-data",
                },
            }
        );
        return data;
    },

    getBeachPosts: async (
        id: number | string,
        page = 0,
        size = 20
    ): Promise<PostDTO[]> => {
        const { data } = await api.get<PostDTO[]>(
            beachRoutes.getPosts(id, page, size)
        );
        return data;
    },

    getAllBeachPosts: async (
        id: number | string
    ): Promise<PostDTO[]> => {
        const { data } = await api.get<PostDTO[]>(beachRoutes.getAllPosts(id), {
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        });
        return data;
    },
};
