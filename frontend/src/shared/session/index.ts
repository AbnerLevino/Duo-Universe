// Ponto único de entrada da sessão: por fora, ninguém precisa saber que ela é 3 arquivos por dentro
// (token.ts, mediaCache.ts, currentUser.ts) — só importa daqui.

export { authedFetch, refreshSession } from './token'
export { loadMediaUrl, forgetMedia } from './mediaCache'
export {
  type User,
  googleLoginUrl,
  clearSession,
  getCurrentUser,
  logout,
  logoutAll,
} from './currentUser'
