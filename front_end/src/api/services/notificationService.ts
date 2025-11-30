import api from "../axios";
import type { AxiosResponse } from "axios";
import type { UserDTO } from "./userService";

export type NotificationType = 'LIKE' | 'COMMENT' | 'REPLY' | 'MENTION' | 'FOLLOW';

export interface NotificationDTO {
    id: number;
    sender: UserDTO;
    type: NotificationType;
    postId: number | null;
    commentId: number | null;
    read: boolean;
    createdAt: string;
    message: string;
}

export interface NotificationCountDTO {
    count: number;
    unreadByType: {
        LIKE: number;
        COMMENT: number;
        REPLY: number;
        MENTION: number;
        FOLLOW: number;
    };
}

export const NotificationService = {
    async getMyNotifications(): Promise<NotificationDTO[]> {
        const res: AxiosResponse<NotificationDTO[]> = await api.get("/notifications/");
        return res.data;
    },

    async getUnreadNotifications(): Promise<NotificationDTO[]> {
        const res: AxiosResponse<NotificationDTO[]> = await api.get("/notifications/");
        return res.data.filter((n) => !n.read);
    },

    async getUnreadCount(): Promise<number> {
        const res: AxiosResponse<{ count: number }> = await api.get("/notifications/count");
        return res.data.count;
    },

    async markAsRead(id: number): Promise<void> {
        await api.put(`/notifications/${id}/read`);
    },

    async markAllAsRead(): Promise<void> {
        try {
            await api.put("/notifications/read-all");
        } catch {
            // Fallback: marcar uma por uma se o endpoint não existir
            const notifications = await this.getUnreadNotifications();
            await Promise.all(notifications.map((n) => this.markAsRead(n.id)));
        }
    },

    async deleteNotification(id: number): Promise<void> {
        try {
            await api.delete(`/notifications/${id}`);
        } catch {
            // Se não houver endpoint de delete, apenas ignora
            console.log('Delete notification not available');
        }
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

    // Helper para obter ícone do tipo de notificação
    getNotificationTypeLabel(type: NotificationType): string {
        const labels: Record<NotificationType, string> = {
            LIKE: 'Curtida',
            COMMENT: 'Comentário',
            REPLY: 'Resposta',
            MENTION: 'Menção',
            FOLLOW: 'Seguidor',
        };
        return labels[type] || 'Notificação';
    },
};
