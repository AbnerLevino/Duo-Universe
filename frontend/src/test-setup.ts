import '@testing-library/jest-dom/vitest'

// O jsdom (o "navegador de mentira" dos testes) não tem URL.createObjectURL, que as fotos usam.
URL.createObjectURL = vi.fn(() => 'blob:foto-de-teste')
URL.revokeObjectURL = vi.fn()
