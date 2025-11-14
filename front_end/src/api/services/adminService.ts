import api from "@/api/axios";

export const AdminService = {

    deleteUser: (userId: number) =>
        api.delete(`/admin/users/${userId}`),

    promoteUser: (userId: number) =>
        api.post(`/admin/users/${userId}/promote`),

    demoteUser: (userId: number) =>
        api.post(`/admin/users/${userId}/demote`),

    banUser: (userId: number) =>
        api.post(`/admin/users/${userId}/ban`),

    unbanUser: (userId: number) =>
        api.post(`/admin/users/${userId}/unban`),

    deletePost: (postId: number) =>
        api.delete(`/admin/posts/${postId}`),

    deleteComment: (commentId: number) =>
        api.delete(`/admin/comments/${commentId}`),

    listAudits: (page = 0, size = 20) =>
        api.get(`/admin/audits?page=${page}&size=${size}`),

    getMetrics: () => api.get(`/admin/metrics`),

    getMetricsByPeriod: (start: string, end: string) =>
        api.get(`/admin/metrics/period?start=${start}&end=${end}`),

    getTopAuthors: (start: string, end: string, limit = 10) =>
        api.get(`/admin/metrics/top-authors?start=${start}&end=${end}&limit=${limit}`),

    getPostsByBeach: (start: string, end: string) =>
        api.get(`/admin/metrics/by-beach?start=${start}&end=${end}`),
};
