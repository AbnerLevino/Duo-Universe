/// <reference types="vitest/config" />
import { fileURLToPath, URL } from 'node:url'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  resolve: {
    // `@/` aponta pra `src/`, pra não precisar de `../../..` dentro de features/.
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
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
        globPatterns: ['**/*.{js,css,html,png,svg,woff2,webmanifest}'],
        // public/images/ guarda arte pesada (ex.: o fundo do login, 13,7 MB) que não precisa funcionar
        // offline — sem isso, o build quebra: o limite padrão de pré-cache é 2 MB por arquivo.
        globIgnores: ['images/**'],
        // O login com Google navega por /oauth2 e /login (em produção, na mesma origem): o service worker
        // não pode responder essas rotas com o index.html.
        navigateFallbackDenylist: [/^\/api/, /^\/oauth2/, /^\/login/],
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
    setupFiles: './src/shared/test/test-setup.ts',
  },
})
