export const adminRoutes = {
    deleteUser: (id: number) => `/admin/users/${id}`,
    deletePost: (id: number) => `/admin/posts/${id}`,
    deleteComment: (id: number) => `/admin/comments/${id}`,
    promote: (id: number) => `/admin/users/${id}/promote`,
    demote: (id: number) => `/admin/users/${id}/demote`,
    ban: (id: number) => `/admin/users/${id}/ban`,
    unban: (id: number) => `/admin/users/${id}/unban`,
    audits: (page = 0, size = 20) => `/admin/audits?page=${page}&size=${size}`,
    metrics: `/admin/metrics`,
    metricsByPeriod: (start: string, end: string) =>
        `/admin/metrics/period?start=${start}&end=${end}`,
    topAuthors: (start: string, end: string, limit = 10) =>
        `/admin/metrics/top-authors?start=${start}&end=${end}&limit=${limit}`,
    postsByBeach: (start: string, end: string) =>
        `/admin/metrics/by-beach?start=${start}&end=${end}`,
};