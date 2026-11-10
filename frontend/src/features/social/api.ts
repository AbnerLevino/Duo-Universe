// Busca, Duollow (seguir), bloqueio e denúncia. Congelado em produção (RF-A11), mas o código e os testes
// continuam existindo.

import { call, withJsonBody, handlePath } from '../../shared/api/client'
import type { Relation, ReportReason } from '../../shared/api/types'

export type ProfileCard = {
  userId: number
  displayName: string
  handle: string
  bio: string | null
  profession: string | null
  avatarMediaId: number | null
}

export type SearchPage = {
  items: { profile: ProfileCard; relation: Relation }[]
  hasMore: boolean
}

/** Busca perfis por nome/handle, paginada. */
export const searchProfiles = (query: string, page: number) =>
  call<SearchPage>(`/api/search/profiles?q=${encodeURIComponent(query)}&page=${page}`)

/** Segue alguém (ou manda pedido, se o perfil for privado). */
export const follow = (handle: string) =>
  call<{ relation: Relation }>(`/api/profiles/${handlePath(handle)}/follow`, { method: 'POST' })
export const unfollow = (handle: string) =>
  call<void>(`/api/profiles/${handlePath(handle)}/follow`, { method: 'DELETE' })

/** Pedidos de quem quer te seguir (perfil privado). */
export const followRequests = () => call<ProfileCard[]>('/api/me/follow-requests')
export const acceptRequest = (handle: string) =>
  call<void>(`/api/me/follow-requests/${handlePath(handle)}/accept`, { method: 'POST' })
export const rejectRequest = (handle: string) =>
  call<void>(`/api/me/follow-requests/${handlePath(handle)}`, { method: 'DELETE' })

export const duollowers = () => call<ProfileCard[]>('/api/me/duollowers')
export const duollowing = () => call<ProfileCard[]>('/api/me/duollowing')
/** Tira alguém da sua lista de duollowers (sem avisar a pessoa). */
export const removeDuollower = (handle: string) =>
  call<void>(`/api/me/duollowers/${handlePath(handle)}`, { method: 'DELETE' })

export const blockUser = (handle: string) =>
  call<void>(`/api/me/blocks/${handlePath(handle)}`, { method: 'PUT' })
export const unblockUser = (handle: string) =>
  call<void>(`/api/me/blocks/${handlePath(handle)}`, { method: 'DELETE' })
export const blockedUsers = () => call<ProfileCard[]>('/api/me/blocks')

/** Denuncia um perfil, pra moderação revisar (H02/H03). */
export const reportProfile = (handle: string, reason: ReportReason, details: string) =>
  call<void>(
    `/api/profiles/${handlePath(handle)}/report`,
    withJsonBody('POST', { reason, details }),
  )
/** Denuncia uma foto específica. */
export const reportPhoto = (id: number, reason: ReportReason, details: string) =>
  call<void>(`/api/media/${id}/report`, withJsonBody('POST', { reason, details }))
