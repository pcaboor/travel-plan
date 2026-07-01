import path from "node:path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api/auth": {
        target: process.env.VITE_AUTH_URL ?? "http://localhost:8081",
        changeOrigin: true,
      },
      "/api/admin": {
        target: process.env.VITE_ADMIN_URL ?? "http://localhost:8082",
        changeOrigin: true,
      },
      "/api/travels": {
        target: process.env.VITE_TRAVEL_URL ?? "http://localhost:8083",
        changeOrigin: true,
      },
      "/api/subscriptions": {
        target: process.env.VITE_ADMIN_URL ?? "http://localhost:8082",
        changeOrigin: true,
      },
      "/api/feedback": {
        target: process.env.VITE_ADMIN_URL ?? "http://localhost:8082",
        changeOrigin: true,
      },
    },
  },
});
