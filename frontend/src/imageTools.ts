// Preparação das fotos no próprio aparelho (RN-24): recorta e reduz antes de enviar, para o upload ser leve
// e o backend nunca receber uma foto de 12 MB do celular. Usa só o <canvas> do navegador, sem biblioteca.

const AVATAR_SIDE = 512
const PHOTO_MAX_SIDE = 1600
// Um pouco abaixo dos limites do backend (1 MB do avatar, 2 MB da foto), para sobrar folga.
const AVATAR_MAX_BYTES = 950_000
const PHOTO_MAX_BYTES = 1_900_000
const QUALITIES = [0.85, 0.7, 0.55, 0.4]

/** O tamanho final de uma imagem reduzida para caber num quadrado de `maxSide`, sem nunca ampliar. */
export function fitWithin(width: number, height: number, maxSide: number) {
  const scale = Math.min(1, maxSide / Math.max(width, height))
  return { width: Math.round(width * scale), height: Math.round(height * scale) }
}

/** O maior quadrado centralizado dentro da imagem (o recorte do avatar). */
export function squareCrop(width: number, height: number) {
  const side = Math.min(width, height)
  return { sx: (width - side) / 2, sy: (height - side) / 2, side }
}

function toBlob(canvas: HTMLCanvasElement, quality: number): Promise<Blob> {
  return new Promise((resolve, reject) =>
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error('Não foi possível preparar a imagem.'))),
      'image/jpeg',
      quality,
    ),
  )
}

async function encodeUnderLimit(canvas: HTMLCanvasElement, maxBytes: number): Promise<Blob> {
  for (const quality of QUALITIES) {
    const blob = await toBlob(canvas, quality)
    if (blob.size <= maxBytes) return blob
  }
  throw new Error('A imagem ficou pesada demais. Tente outra foto.')
}

export async function prepareAvatar(file: File): Promise<Blob> {
  const bitmap = await createImageBitmap(file)
  const { sx, sy, side } = squareCrop(bitmap.width, bitmap.height)
  const size = Math.min(AVATAR_SIDE, side)
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  canvas.getContext('2d')!.drawImage(bitmap, sx, sy, side, side, 0, 0, size, size)
  bitmap.close()
  return encodeUnderLimit(canvas, AVATAR_MAX_BYTES)
}

export async function preparePhoto(file: File): Promise<Blob> {
  const bitmap = await createImageBitmap(file)
  const { width, height } = fitWithin(bitmap.width, bitmap.height, PHOTO_MAX_SIDE)
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  canvas.getContext('2d')!.drawImage(bitmap, 0, 0, width, height)
  bitmap.close()
  return encodeUnderLimit(canvas, PHOTO_MAX_BYTES)
}
