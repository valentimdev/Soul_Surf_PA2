export const userRoutes = {
    base: "/users",

    getById: (id: number | string) => `${userRoutes.base}/${id}`,
    getMe: () => `${userRoutes.base}/me`,
    updateProfile: () => `${userRoutes.base}/me/upload`,

    follow: (id: number | string) => `${userRoutes.base}/${id}/follow`,
    unfollow: (id: number | string) => `${userRoutes.base}/${id}/follow`,

    getFollowers: (id: number | string) => `${userRoutes.base}/${id}/followers`,
    getFollowing: (id: number | string) => `${userRoutes.base}/${id}/following`,

    hello: () => `${userRoutes.base}/hello`,
};