// O próprio perfil (ler, editar, avatar) e o perfil público de outra pessoa.

import { call, withJsonBody, fileForm, handlePath } from '../../shared/api/client'
import type {
  MaritalStatus,
  ProfileVisibility,
  Relation,
  MyProfile,
  MediaSummary,
} from '../../shared/api/types'

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

/** O perfil de quem está logado. */
export const myProfile = () => call<MyProfile>('/api/me/profile')
export const updateProfile = (update: ProfileUpdate) =>
  call<MyProfile>('/api/me/profile', withJsonBody('PUT', update))

/** O perfil público de alguém, pelo @handle (inclui a relação com quem está vendo). */
export const getProfile = (handle: string) =>
  call<PublicProfile>(`/api/profiles/${handlePath(handle)}`)

export const uploadAvatar = (blob: Blob) =>
  call<MediaSummary>('/api/me/avatar', { method: 'POST', body: fileForm(blob) })
export const removeAvatar = () => call<void>('/api/me/avatar', { method: 'DELETE' })
