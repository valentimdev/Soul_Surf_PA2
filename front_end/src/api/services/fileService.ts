// src/api/services/fileService.ts
import api from "../axios";
import { fileRoutes } from "../routes/file";

export type FileListResponse = string[]; // lista de URLs ou nomes de arquivos
export type UploadResponse = string;     // mensagem do backend

export type UploadFileRequest = {
  file: File;
};

export const FileService = {
  upload: async ({ file }: UploadFileRequest): Promise<UploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);

    const { data } = await api.post<UploadResponse>(fileRoutes.upload(), formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return data;
  },

  list: async (): Promise<FileListResponse> => {
    const { data } = await api.get<FileListResponse>(fileRoutes.list());
    return data;
  },
};
