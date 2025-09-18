// src/api/services/postService.ts
import api from "../axios";
import { postRoutes } from "../routes/post";
import type {UserDTO} from "./userService";

// DTOs
export type PostDTO = {
  id: number;
  titulo: string;
  descricao: string;
  caminhoFoto?: string;
  data: string; // LocalDateTime do backend vira string
  usuario: UserDTO;
};

export type MessageResponse = {
  message: string;
};

export type CreatePostRequest = {
  titulo: string;
  descricao: string;
  foto?: File;
};

export const PostService = {
  create: async ({ titulo, descricao, foto }: CreatePostRequest): Promise<MessageResponse> => {
    const formData = new FormData();
    formData.append("titulo", titulo);
    formData.append("descricao", descricao);
    if (foto) formData.append("foto", foto);

    const { data } = await api.post<MessageResponse>(postRoutes.create(), formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return data;
  },

  // futuros endpoints
  getById: async (id: number | string): Promise<PostDTO> => {
    const { data } = await api.get<PostDTO>(postRoutes.getById(id));
    return data;
  },

  list: async (): Promise<PostDTO[]> => {
    const { data } = await api.get<PostDTO[]>(postRoutes.list());
    return data;
  },
};