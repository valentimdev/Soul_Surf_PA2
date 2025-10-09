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
    create: async ({ descricao, publico, beachId, foto }: CreatePostRequest): Promise<MessageResponse> => {
        const formData = new FormData();
        formData.append("descricao", descricao);
        formData.append("publico", String(publico));
        if (beachId) formData.append("beachId", beachId.toString());
        if (foto) formData.append("foto", foto);

        const { data } = await api.post<MessageResponse>(postRoutes.create(), formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });
        return data;
    },

    getById: async (id: number | string): Promise<PostDTO> => {
        const { data } = await api.get<PostDTO>(postRoutes.getById(id));
        return data;
    },

    list: async (): Promise<PostDTO[]> => {
        const { data } = await api.get<PostDTO[]>(postRoutes.list());
        return data;
    },
};