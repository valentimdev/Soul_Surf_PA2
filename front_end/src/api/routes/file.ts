export const fileRoutes = {
    base: "/files",

    upload: () => `${fileRoutes.base}/upload`,
    list: () => `${fileRoutes.base}/list`,
};