// Chamadas HTTP do vínculo (Etapa 14). Tudo passa por authedFetch (session.ts), que já cuida do token de
// acesso e da renovação; aqui só montamos o caminho, o corpo e interpretamos a resposta.

import { authedFetch } from './session'

export type Channel = 'HANDLE' | 'LINK'
export type PairingState = 'PENDING' | 'CONFIRMED' | 'CANCELLED'
export type CoupleState = 'ACTIVE' | 'PAUSED' | 'ENDED'
export type StoryStep =
  'HOW_WE_MET' | 'TIME_BEFORE_DATING' | 'WHAT_I_THOUGHT_THEN' | 'WHAT_I_THINK_NOW'

export const STORY_STEPS: StoryStep[] = [
  'HOW_WE_MET',
  'TIME_BEFORE_DATING',
  'WHAT_I_THOUGHT_THEN',
  'WHAT_I_THINK_NOW',
]

export type Pairing = {
  id: number
  initiatorUserId: number
  inviteeUserId: number | null
  channel: Channel
  state: PairingState
  expiresAt: string
  createdAt: string
  coupleId: number | null
  initiatorConfirmedAt: string | null
  inviteeConfirmedAt: string | null
  dateProposedByUserId: number | null
  proposedKnownSinceDate: string | null
  proposedDatingSinceDate: string | null
  datesAcceptedAt: string | null
}

export type Couple = {
  id: number
  state: CoupleState
  knownSinceDate: string | null
  datingSinceDate: string | null
  pausedAt: string | null
  endedAt: string | null
}

export type Story = {
  pairingId: number
  userId: number
  howWeMet: string | null
  timeBeforeDating: string | null
  whatIThoughtThen: string | null
  whatIThinkNow: string | null
}

export type IssuedInvite = { pairingId: number; code: string; expiresAt: string }

/** O que o GlobalExceptionHandler do backend devolve (ProblemDetail) quando uma regra de negócio barra a ação. */
export class ApiError extends Error {
  status: number

  constructor(status: number, detail: string) {
    super(detail)
    this.status = status
  }
}

async function call<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await authedFetch(path, init)
  if (!response) throw new ApiError(401, 'Sessão encerrada. Entre de novo.')
  if (response.status === 204) return null as T
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null
    throw new ApiError(response.status, problem?.detail ?? 'Não foi possível completar a ação.')
  }
  return (await response.json()) as T
}

const withJsonBody = (method: string, body: unknown): RequestInit => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

export const myCouple = () => call<Couple | null>('/api/couples/mine')
export const getCouple = (id: number) => call<Couple>(`/api/couples/${id}`)
export const pauseCouple = (id: number) =>
  call<Couple>(`/api/couples/${id}/pause`, { method: 'POST' })
export const resumeCouple = (id: number) =>
  call<Couple>(`/api/couples/${id}/resume`, { method: 'POST' })
export const leaveCouple = (id: number) =>
  call<void>(`/api/couples/${id}/leave`, { method: 'POST' })

export const myPairings = () => call<Pairing[]>('/api/pairings/mine')
export const getPairing = (id: number) => call<Pairing>(`/api/pairings/${id}`)

export const inviteByHandle = (handle: string) =>
  call<Pairing>('/api/pairings/invite-by-handle', withJsonBody('POST', { handle }))

export const inviteByLink = () =>
  call<IssuedInvite>('/api/pairings/invite-by-link', { method: 'POST' })

export const redeemByLink = (code: string) =>
  call<Pairing>('/api/pairings/redeem', withJsonBody('POST', { code }))

export const confirmPairing = (id: number) =>
  call<Pairing>(`/api/pairings/${id}/confirm`, { method: 'POST' })
export const cancelPairing = (id: number) =>
  call<void>(`/api/pairings/${id}/cancel`, { method: 'POST' })

export const saveStoryStep = (pairingId: number, step: StoryStep, text: string) =>
  call<void>(`/api/pairings/${pairingId}/story/${step}`, withJsonBody('PUT', { text }))

export const partnerStory = (pairingId: number) =>
  call<Story | null>(`/api/pairings/${pairingId}/partner-story`)

export const proposeDates = (pairingId: number, knownSinceDate: string, datingSinceDate: string) =>
  call<void>(
    `/api/pairings/${pairingId}/dates`,
    withJsonBody('PUT', { knownSinceDate, datingSinceDate }),
  )

export const acceptDates = (pairingId: number) =>
  call<void>(`/api/pairings/${pairingId}/dates/accept`, { method: 'POST' })

export const chooseHandle = (handle: string) =>
  call<{ id: number; email: string; displayName: string; handle: string | null }>(
    '/api/me/handle',
    withJsonBody('PUT', { handle }),
  )

// ---------- Perfil, busca, Duollow, bloqueio, denúncia e fotos (Épico 3) ----------

export type MaritalStatus = 'SINGLE' | 'DATING' | 'MARRIED' | 'DIVORCED' | 'WIDOWED'
export type ProfileVisibility = 'PUBLIC' | 'PRIVATE'
export type Relation = 'NONE' | 'REQUESTED' | 'FOLLOWING'
export type MediaVisibility = 'ONLY_ME' | 'DUOLLOWERS' | 'PUBLIC'
export type ReportReason =
  | 'NUDITY_OR_SEXUAL'
  | 'HARASSMENT_OR_THREAT'
  | 'FAKE_PROFILE'
  | 'SUSPECTED_MINOR'
  | 'SPAM'
  | 'OTHER'

export type MyProfile = {
  id: number
  displayName: string
  handle: string | null
  bio: string | null
  profession: string | null
  maritalStatus: MaritalStatus
  profileVisibility: ProfileVisibility
  avatarMediaId: number | null
  hidden: boolean
}

export type ProfileUpdate = {
  displayName: string
  bio: string
  profession: string
  maritalStatus: MaritalStatus
  profileVisibility: ProfileVisibility
}

export type PublicProfile = {
  userId: number
  displayName: string
  handle: string
  bio: string | null
  profession: string | null
  maritalStatus: MaritalStatus
  profileVisibility: ProfileVisibility
  avatarMediaId: number | null
  duollowers: number
  duollowing: number
  relation: Relation
  self: boolean
  canSeeContent: boolean
}

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

export type MediaSummary = {
  id: number
  ownerUserId: number
  kind: 'AVATAR' | 'PHOTO'
  visibility: MediaVisibility
  caption: string | null
  contentType: string
  sizeBytes: number
  createdAt: string
}

/** A mensagem que o backend mandou (ProblemDetail) ou, se for outro erro, uma frase padrão. */
export const errorMessage = (err: unknown, fallback: string) =>
  err instanceof ApiError ? err.message : fallback

const handlePath = (handle: string) => encodeURIComponent(handle)

export const myProfile = () => call<MyProfile>('/api/me/profile')
export const updateProfile = (update: ProfileUpdate) =>
  call<MyProfile>('/api/me/profile', withJsonBody('PUT', update))

export const getProfile = (handle: string) =>
  call<PublicProfile>(`/api/profiles/${handlePath(handle)}`)
export const searchProfiles = (query: string, page: number) =>
  call<SearchPage>(`/api/search/profiles?q=${encodeURIComponent(query)}&page=${page}`)

export const follow = (handle: string) =>
  call<{ relation: Relation }>(`/api/profiles/${handlePath(handle)}/follow`, { method: 'POST' })
export const unfollow = (handle: string) =>
  call<void>(`/api/profiles/${handlePath(handle)}/follow`, { method: 'DELETE' })

export const followRequests = () => call<ProfileCard[]>('/api/me/follow-requests')
export const acceptRequest = (handle: string) =>
  call<void>(`/api/me/follow-requests/${handlePath(handle)}/accept`, { method: 'POST' })
export const rejectRequest = (handle: string) =>
  call<void>(`/api/me/follow-requests/${handlePath(handle)}`, { method: 'DELETE' })
export const duollowers = () => call<ProfileCard[]>('/api/me/duollowers')
export const duollowing = () => call<ProfileCard[]>('/api/me/duollowing')
export const removeDuollower = (handle: string) =>
  call<void>(`/api/me/duollowers/${handlePath(handle)}`, { method: 'DELETE' })

export const blockUser = (handle: string) =>
  call<void>(`/api/me/blocks/${handlePath(handle)}`, { method: 'PUT' })
export const unblockUser = (handle: string) =>
  call<void>(`/api/me/blocks/${handlePath(handle)}`, { method: 'DELETE' })
export const blockedUsers = () => call<ProfileCard[]>('/api/me/blocks')

export const reportProfile = (handle: string, reason: ReportReason, details: string) =>
  call<void>(
    `/api/profiles/${handlePath(handle)}/report`,
    withJsonBody('POST', { reason, details }),
  )
export const reportPhoto = (id: number, reason: ReportReason, details: string) =>
  call<void>(`/api/media/${id}/report`, withJsonBody('POST', { reason, details }))

function fileForm(blob: Blob, extra: Record<string, string> = {}) {
  const form = new FormData()
  form.append('file', blob, 'imagem.jpg')
  for (const [key, value] of Object.entries(extra)) form.append(key, value)
  return form
}

export const uploadAvatar = (blob: Blob) =>
  call<MediaSummary>('/api/me/avatar', { method: 'POST', body: fileForm(blob) })
export const removeAvatar = () => call<void>('/api/me/avatar', { method: 'DELETE' })

export const uploadPhoto = (blob: Blob, caption: string, visibility: MediaVisibility) =>
  call<MediaSummary>('/api/me/photos', {
    method: 'POST',
    body: fileForm(blob, { caption, visibility }),
  })
export const photosOf = (userId: number) =>
  call<MediaSummary[]>(`/api/media/users/${userId}/photos`)
export const editPhoto = (id: number, caption: string, visibility: MediaVisibility) =>
  call<MediaSummary>(`/api/media/${id}`, withJsonBody('PATCH', { caption, visibility }))
export const deletePhoto = (id: number) => call<void>(`/api/media/${id}`, { method: 'DELETE' })
