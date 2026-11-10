// As fotos só saem do backend com o token de acesso, e um <img> comum não envia o cabeçalho Authorization.
// Por isso o arquivo é buscado com authedFetch e vira um endereço local (blob:). Guardamos o endereço por id
// para não baixar a mesma foto várias vezes; e esquecemos tudo ao sair, para a próxima pessoa que entrar
// neste aparelho nunca herdar a foto de alguém que ela não pode ver.

import { authedFetch } from './token'

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

/** Esquece todas as fotos do cache (parte do "sair"). */
export function clearMediaCache() {
  for (const id of [...mediaUrls.keys()]) forgetMedia(id)
}
