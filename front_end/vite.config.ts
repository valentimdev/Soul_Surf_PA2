import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from "path"
import tailwindcss from "@tailwindcss/vite"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  define: {
    global: {}, // ✅ Corrige erro "global is not defined" no sockjs-client
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080", // ✅ envia /api → backend Spring Boot
        changeOrigin: true,
        secure: false,
      },
      "/ws": {
        target: "http://localhost:8080", // ✅ envia /ws → WebSocket
        ws: true,
        changeOrigin: true,
      },
    },
  },
})
