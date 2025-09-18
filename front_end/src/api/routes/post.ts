// src/api/routes/post.ts
export const postRoutes = {
  base: "/posts",

  create: () => `${postRoutes.base}`,         // POST /posts
  getById: (id: number | string) => `${postRoutes.base}/${id}`, // futuro GET /posts/:id
  list: () => `${postRoutes.base}`,           // futuro GET /posts
};