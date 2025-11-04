import api from "../axios";
import type { AxiosResponse } from "axios";
import type { UserDTO } from "./userService";

export interface NotificationDTO {
    id: number;
    sender: UserDTO;
    type: string;
    postId: number | null;
    commentId: number | null;
    read: boolean;
    createdAt: string;
    message: string;
}

export const NotificationService = {
    async getMyNotifications(): Promise<NotificationDTO[]> {
        const res: AxiosResponse<NotificationDTO[]> = await api.get("/notifications/");
        return res.data;
    },

    async getUnreadCount(): Promise<number> {
        const res: AxiosResponse<{ count: number }> = await api.get("/notifications/count");
        return res.data.count;
    },

    async markAsRead(id: number): Promise<void> {
        await api.put(`/notifications/${id}/read`);
    },

    async comment(postId: number, commentId: number): Promise<void> {
        await api.post(`/notifications/comment`, null, {
            params: { postId, commentId },
        });
    },

    async reply(postId: number, commentId: number, parentCommentId: number): Promise<void> {
        await api.post(`/notifications/reply`, null, {
            params: { postId, commentId, parentCommentId },
        });
    },

    async mention(recipientUsername: string, postId: number, commentId: number | null = null): Promise<void> {
        const params: Record<string, any> = { recipientUsername, postId };
        if (commentId !== null) params.commentId = commentId;

        await api.post(`/notifications/mention`, null, {
            params: { ...params }
        });
    },
};
