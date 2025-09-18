// src/api/services/postService.ts
import api from "../axios";
import { postRoutes } from "../routes/posts";

// Tipos dos dados do request
export type CreatePostRequest = {
  titulo: string;
  descricao: string;
  foto?: File; // foto é opcional
};

export const PostService = {
  create: async ({ titulo, descricao, foto }: CreatePostRequest) => {
    const formData = new FormData();
    formData.append("titulo", titulo);
    formData.append("descricao", descricao);
    if (foto) {
      formData.append("foto", foto);
    }

    const { data } = await api.post(postRoutes.create(), formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return data; // deve retornar MessageResponse
  },
};
