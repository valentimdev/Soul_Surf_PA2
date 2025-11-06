export const postRoutes = {
    base: "/posts",

    create: () => `${postRoutes.base}`,
    getById: (id: number | string) => `${postRoutes.base}/${id}`,
    list: () => `${postRoutes.base}/home`,
    byUser: (email: string) => `${postRoutes.base}/user/${email}`,
    update: (id: number | string) => `${postRoutes.base}/${id}`,
    delete: (id: number | string) => `${postRoutes.base}/${id}`,
};
