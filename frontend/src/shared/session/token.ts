// O token de acesso (JWT de 15 min, RA-04) e a chamada autenticada à API. Vive só na memória: nunca em
// localStorage nem em cookie legível. O token de renovação fica num cookie HttpOnly que o JavaScript não
// enxerga; só o backend o usa.

let accessToken: string | null = null
let inflightRefresh: Promise<string | null> | null = null

async function requestNewAccessToken(): Promise<string | null> {
  try {
    const response = await fetch('/api/auth/refresh', { method: 'POST' })
    if (!response.ok) return null
    const body = (await response.json()) as { accessToken: string }
    return body.accessToken
  } catch {
    return null
  }
}

/**
 * Pede um token de acesso novo. Chamadas simultâneas dividem a mesma requisição: o refresh é de uso único
 * (RA-05), então duas chamadas com o mesmo cookie seriam lidas como roubo e derrubariam a sessão.
 */
export function refreshSession(): Promise<string | null> {
  if (!inflightRefresh) {
    inflightRefresh = requestNewAccessToken()
      .then((token) => {
        accessToken = token
        return token
      })
      .finally(() => {
        inflightRefresh = null
      })
  }
  return inflightRefresh
}

/** Esquece o token desta aba (parte do "sair" — a outra parte é esquecer o cache de fotos, em mediaCache.ts). */
export function resetAccessToken() {
  accessToken = null
}

/** Chama a API com o token de acesso; se ele venceu, renova uma vez e repete. Devolve null se a sessão acabou. */
export async function authedFetch(path: string, init: RequestInit = {}): Promise<Response | null> {
  const call = (token: string) =>
    fetch(path, { ...init, headers: { ...init.headers, Authorization: `Bearer ${token}` } })

  let token = accessToken ?? (await refreshSession())
  if (!token) return null
  let response = await call(token)
  if (response.status === 401) {
    token = await refreshSession()
    if (!token) return null
    response = await call(token)
  }
  return response
}
