import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from "path";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  define: {
    global: {},
  },
  server: {
    proxy: {
      "/api": {
        target: "https://soulsurfpa2-production.up.railway.app",
        changeOrigin: true,
        secure: false,
      },
      "/ws": {
        target: "https://soulsurfpa2-production.up.railway.app",
        ws: true,
        changeOrigin: true,
      },
    },
  },
});