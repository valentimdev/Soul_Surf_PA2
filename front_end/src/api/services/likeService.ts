import api from "@/api/axios";
import { postRoutes } from "@/api/routes/post";

export interface LikeResponse {
    liked: boolean;
    likesCount: number;
    message: string;
}

export const LikeService = {
    async toggleLike(postId: number): Promise<LikeResponse> {
        const response = await api.post<LikeResponse>(`${postRoutes.base}/${postId}/likes`);
        return response.data;
    },

    async getLikesCount(postId: number): Promise<number> {
        const response = await api.get<{ count: number }>(`${postRoutes.base}/${postId}/likes/count`);
        return response.data.count;
    },

    async hasUserLiked(postId: number): Promise<boolean> {
        const response = await api.get<{ liked: boolean }>(`${postRoutes.base}/${postId}/likes/status`);
        return response.data.liked;
    },
};

