// Quem está logado agora, entrar com Google e sair (RA-04, H4).

import { authedFetch, resetAccessToken } from './token'
import { clearMediaCache } from './mediaCache'

export type User = { id: number; email: string; displayName: string; handle: string | null }

// Em desenvolvimento o login com Google precisa começar no backend (é lá que está o redirect cadastrado
// no Google); em produção front e back dividem a origem.
const BACKEND = import.meta.env.DEV ? 'http://localhost:8080' : ''
export const googleLoginUrl = `${BACKEND}/oauth2/authorization/google`

/** Esquece token e cache de fotos — usado ao sair, de uma vez só. */
export function clearSession() {
  resetAccessToken()
  clearMediaCache()
}

export async function getCurrentUser(): Promise<User | null> {
  const response = await authedFetch('/api/me')
  return response?.ok ? ((await response.json()) as User) : null
}

/** "Sair deste aparelho" (H4). */
export async function logout() {
  clearSession()
  await fetch('/api/auth/logout', { method: 'POST' })
}

/** "Sair de todos os aparelhos" (H4). */
export async function logoutAll() {
  await authedFetch('/api/auth/logout-all', { method: 'POST' })
  clearSession()
}
