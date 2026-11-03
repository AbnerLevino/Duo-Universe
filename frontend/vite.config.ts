/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'Duo Universe',
        short_name: 'Duo',
        description: 'Two lives. One universe.',
        lang: 'pt-BR',
        display: 'standalone',
        start_url: '/',
        background_color: '#14101f',
        theme_color: '#14101f',
        icons: [
          { src: 'pwa-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'pwa-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Só arquivos estáticos do app entram no cache. A API NUNCA é cacheada:
        // são dados privados do casal (seção 15) e não há runtimeCaching configurado.
        globPatterns: ['**/*.{js,css,html,png,svg,webmanifest}'],
        navigateFallbackDenylist: [/^\/api/],
      },
    }),
  ],
  server: {
    // Mesma origem do backend, como em produção (DEC-025): o front usa caminhos relativos
    // (/api/...) e o proxy repassa ao Spring. Sem CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test-setup.ts',
  },
})
