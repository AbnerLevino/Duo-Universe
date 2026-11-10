import '@testing-library/jest-dom/vitest'
import { Blob } from 'node:buffer'

// O jsdom tem seu próprio `Blob`, mas o `Response`/`fetch` dos testes vêm do Node (undici), que espera um
// Blob com `.stream()` do jeito do Node — o do jsdom não tem. Sem isso, `new Response(new Blob(...))` quebra
// com "object.stream is not a function" (bug conhecido da combinação jsdom + undici).
globalThis.Blob = Blob as unknown as typeof globalThis.Blob

// O jsdom (o "navegador de mentira" dos testes) não tem URL.createObjectURL, que as fotos usam.
URL.createObjectURL = vi.fn(() => 'blob:foto-de-teste')
URL.revokeObjectURL = vi.fn()

// O jsdom não tem PointerEvent (o gesto de puxar o modal usa ele): o MouseEvent já traz clientY e serve nos testes.
if (!('PointerEvent' in window)) {
  Object.defineProperty(window, 'PointerEvent', { value: MouseEvent })
}

// RF-A11: a camada social está congelada no produto, mas as telas dela continuam existindo e têm testes; por
// padrão os testes rodam com ela ligada, e `features.test.tsx` cobre o modo congelado.
beforeEach(() => {
  vi.stubEnv('VITE_FEATURE_SOCIAL', 'true')
})
