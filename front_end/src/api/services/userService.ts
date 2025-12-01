import api from "../axios";
import { userRoutes } from "../routes/user";
import type { MessageResponse } from "@/api/services/postService.ts";
import type { BeachDTO } from "@/api/services/beachService.ts";

class CommentDTO {}

// DTOs do backend
export type PostDTO = {
    id: number;
    descricao: string;
    caminhoFoto?: string;
    data: string;
    usuario: UserDTO;
    publico: boolean;
    beach?: BeachDTO;
    comments: CommentDTO[];
    likesCount?: number;
    commentsCount?: number;
    likedByCurrentUser?: boolean;
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
    isFollowing?: boolean;
    admin?: boolean;
};

export const UserService = {
    getUserById: async (id: number | string): Promise<UserDTO> => {
        const { data } = await api.get<UserDTO>(userRoutes.getById(id));
        return data;
    },

    getUserByUsername: async (username: string): Promise<UserDTO> => {
        const { data } = await api.get<UserDTO>(userRoutes.getByUsername(username));
        return data;
    },

    getMe: async (): Promise<UserDTO> => {
        const { data } = await api.get<UserDTO>(userRoutes.getMe());
        return data;
    },

    updateProfile: async (formData: FormData): Promise<UserDTO> => {
        const { data } = await api.put<UserDTO>(userRoutes.updateProfile(), formData, {
            headers: {
                "Content-Type": "multipart/form-data",
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

    getAllUsersPaginated: async (page = 0, size = 30): Promise<{ content: UserDTO[], totalPages: number, totalElements: number, number: number, last: boolean, first: boolean }> => {
        const { data } = await api.get(`${userRoutes.base}?page=${page}&size=${size}`);
        return data;
    },

    searchUsers: async (query: string, page = 0, size = 30): Promise<{ content: UserDTO[], totalPages: number, totalElements: number, number: number, last: boolean, first: boolean }> => {
        const { data } = await api.get(`${userRoutes.base}/search?query=${query}&page=${page}&size=${size}`);
        return data;
    },

    hello: async (): Promise<string> => {
        const { data } = await api.get(userRoutes.hello());
        return data;
    },
};
