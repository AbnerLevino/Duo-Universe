// A sessão do Duo no navegador (RA-04).
// O token de acesso (JWT de 15 min) vive só na memória: nunca em localStorage nem em cookie legível.
// O token de renovação fica num cookie HttpOnly que o JavaScript não enxerga; só o backend o usa.

export type User = { id: number; email: string; displayName: string; handle: string | null }

// Em desenvolvimento o login com Google precisa começar no backend (é lá que está o redirect cadastrado
// no Google); em produção front e back dividem a origem.
const BACKEND = import.meta.env.DEV ? 'http://localhost:8080' : ''
export const googleLoginUrl = `${BACKEND}/oauth2/authorization/google`

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

// As fotos só saem do backend com o token de acesso, e um <img> comum não envia o cabeçalho Authorization.
// Por isso o arquivo é buscado com authedFetch e vira um endereço local (blob:). Guardamos o endereço por id
// para não baixar a mesma foto várias vezes; e esquecemos tudo ao sair, para a próxima pessoa que entrar
// neste aparelho nunca herdar a foto de alguém que ela não pode ver.
const mediaUrls = new Map<number, Promise<string | null>>()

export function loadMediaUrl(mediaId: number): Promise<string | null> {
  let url = mediaUrls.get(mediaId)
  if (!url) {
    url = authedFetch(`/api/media/${mediaId}`).then(async (response) =>
      response?.ok ? URL.createObjectURL(await response.blob()) : null,
    )
    mediaUrls.set(mediaId, url)
  }
  return url
}

/** Esquece uma foto do cache (depois de apagar ou trocar). */
export function forgetMedia(mediaId: number) {
  const url = mediaUrls.get(mediaId)
  mediaUrls.delete(mediaId)
  url?.then((objectUrl) => objectUrl && URL.revokeObjectURL(objectUrl))
}

export function clearSession() {
  accessToken = null
  for (const id of [...mediaUrls.keys()]) forgetMedia(id)
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
