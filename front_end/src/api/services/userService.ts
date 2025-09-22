import api from "../axios";
import { userRoutes } from "../routes/user";

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
    fotoPerfil?: string;
    fotoCapa?: string;
    seguidoresCount: number;
    seguindoCount: number;
    posts: PostDTO[];
};

export type MessageResponse = {
    message: string;
};

export type UpdateProfileRequest = {
    fotoPerfil?: File;
    fotoCapa?: File;
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

    updateProfile: async ({ fotoPerfil, fotoCapa }: UpdateProfileRequest): Promise<MessageResponse> => {
        const formData = new FormData();
        if (fotoPerfil) formData.append("fotoPerfil", fotoPerfil);
        if (fotoCapa) formData.append("fotoCapa", fotoCapa);

        const { data } = await api.put<MessageResponse>(userRoutes.updateProfile(), formData, {
            headers: { "Content-Type": "multipart/form-data" },
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

    getFollowers: async (username: string): Promise<UserDTO[]> => {
        const { data } = await api.get<UserDTO[]>(userRoutes.getFollowers(username));
        return data;
    },

    getFollowing: async (username: string): Promise<UserDTO[]> => {
        const { data } = await api.get<UserDTO[]>(userRoutes.getFollowing(username));
        return data;
    },

    hello: async (): Promise<string> => {
        const { data } = await api.get(userRoutes.hello());
        return data;
    },
};
