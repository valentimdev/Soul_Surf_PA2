export const userRoutes = {
    base: "/users",

    getById: (id: number | string) => `${userRoutes.base}/${id}`,
    getMe: () => `${userRoutes.base}/me`,
    updateProfile: () => `${userRoutes.base}/me/upload`,

    follow: (id: number | string) => `${userRoutes.base}/${id}/follow`,
    unfollow: (id: number | string) => `${userRoutes.base}/${id}/follow`,

    getFollowers: (username: string) => `${userRoutes.base}/${username}/followers`,
    getFollowing: (username: string) => `${userRoutes.base}/${username}/following`,

    hello: () => `${userRoutes.base}/hello`,
};