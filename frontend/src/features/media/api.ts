// Enviar, listar, editar e apagar fotos (fora o avatar, que é um caso particular de profile/api.ts).

import { call, withJsonBody, fileForm } from '../../shared/api/client'
import type { MediaVisibility, MediaSummary } from '../../shared/api/types'

/** Envia uma foto nova, já comprimida no aparelho. */
export const uploadPhoto = (blob: Blob, caption: string, visibility: MediaVisibility) =>
  call<MediaSummary>('/api/me/photos', {
    method: 'POST',
    body: fileForm(blob, { caption, visibility }),
  })
/** As fotos de alguém que você tem permissão de ver. */
export const photosOf = (userId: number) =>
  call<MediaSummary[]>(`/api/media/users/${userId}/photos`)
export const editPhoto = (id: number, caption: string, visibility: MediaVisibility) =>
  call<MediaSummary>(`/api/media/${id}`, withJsonBody('PATCH', { caption, visibility }))
export const deletePhoto = (id: number) => call<void>(`/api/media/${id}`, { method: 'DELETE' })
