export const beachRoutes = {
    base: "/beaches",

    getAll: () => `${beachRoutes.base}`,
    getById: (id: number | string) => `${beachRoutes.base}/${id}`,
    create: () => `${beachRoutes.base}`,
    getPosts: (id: number | string, page = 0, size = 20) =>
        `${beachRoutes.base}/${id}/posts?page=${page}&size=${size}`,
    getAllPosts: (id: number | string) => `${beachRoutes.base}/${id}/all-posts`,
};
