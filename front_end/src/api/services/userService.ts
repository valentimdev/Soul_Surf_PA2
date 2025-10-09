import api from "../axios";
import { userRoutes } from "../routes/user";
import type {MessageResponse} from "@/api/services/postService.ts";

// DTOs do backend
export type PostDTO = {
    id: number;
    titulo: string;
    descricao: string;
    caminhoFoto?: string;
    data: string;
    usuario: UserDTO;
};

export type UserDTO = {
    id: number;
    username: string;
    email: string;
    bio: string;
    fotoPerfil?: string;
    fotoCapa?: string;
    seguidoresCount: number;
    seguindoCount: number;
    posts: PostDTO[];
};

export const UserService = {
    getUserById: async (id: number | string): Promise<UserDTO> => {
        const { data } = await api.get<UserDTO>(userRoutes.getById(id));
        return data;
    },

    getMe: async (): Promise<UserDTO> => {
        const { data } = await api.get<UserDTO>(userRoutes.getMe());
        return data;
    },

    updateProfile: async (formData: FormData): Promise<UserDTO> => {
        const { data } = await api.put<UserDTO>(userRoutes.updateProfile(), formData, {
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        });
        return data;
    },

    follow: async (id: number | string): Promise<MessageResponse> => {
        const { data } = await api.post<MessageResponse>(userRoutes.follow(id));
        return data;
    },

    unfollow: async (id: number | string): Promise<MessageResponse> => {
        const { data } = await api.delete<MessageResponse>(userRoutes.unfollow(id));
        return data;
    },

    getFollowers: async (id: number): Promise<UserDTO[]> => {
        const { data } = await api.get<UserDTO[]>(userRoutes.getFollowers(id));
        return data;
    },

    getFollowing: async (id: number): Promise<UserDTO[]> => {
        const { data } = await api.get<UserDTO[]>(userRoutes.getFollowing(id));
        return data;
    },

    hello: async (): Promise<string> => {
        const { data } = await api.get(userRoutes.hello());
        return data;
    },
};
