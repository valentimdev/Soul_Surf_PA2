import api from "../axios";
import { postRoutes } from "../routes/post";
import type {UserDTO} from "./userService";

export type PostDTO = {
    id: number;
    descricao: string;
    caminhoFoto?: string;
    data: string;
    usuario: UserDTO;
    publico: boolean;
    beach?: {
        id: number;
        nome: string;
        descricao: string;
        localizacao: string;
        caminhoFoto?: string;
    };
    likesCount?: number;
    commentsCount: number;
    likedByCurrentUser?: boolean;
};

export type MessageResponse = {
    message: string;
};

export type CreatePostRequest = {
    descricao: string;
    publico: boolean;
    beachId?: number;
    foto?: File;
};

export const PostService = {
    create: async ({ descricao, publico, beachId, foto }: CreatePostRequest): Promise<PostDTO> => {
        const formData = new FormData();
        formData.append("descricao", descricao);
        formData.append("publico", String(publico));
        if (beachId) formData.append("beachId", beachId.toString());
        if (foto) formData.append("foto", foto);

        const { data } = await api.post<PostDTO>(postRoutes.create(), formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });
        return data;
    },

    getById: async (id: number | string): Promise<PostDTO> => {
        const { data } = await api.get<PostDTO>(postRoutes.getById(id));
        return data;
    },
    list: async (page = 0, size = 20): Promise<{ content: PostDTO[], totalPages: number, totalElements: number, number: number, last: boolean, first: boolean }> => {
        const { data } = await api.get(`${postRoutes.base}/home?page=${page}&size=${size}`);
        return data;
    },

    getFollowingPosts: async (page = 0, size = 20): Promise<{ content: PostDTO[], totalPages: number, totalElements: number, number: number, last: boolean, first: boolean }> => {
        const { data } = await api.get(`${postRoutes.base}/following?page=${page}&size=${size}`);
        return data;
    },

    getPostsByUser: async (email: string, page = 0, size = 20): Promise<{ content: PostDTO[], totalPages: number, totalElements: number, number: number, last: boolean, first: boolean }> => {
        const { data } = await api.get(`${postRoutes.base}/user?email=${email}&page=${page}&size=${size}`);
        return data;
    },
};