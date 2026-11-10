import { useEffect, useState } from 'react'
import { loadMediaUrl } from '@/shared/session'
import styles from './AuthedImage.module.css'

type Props = { mediaId: number; alt: string; className?: string }

/** Uma foto do Duo: o arquivo é buscado com o token (session.ts) e mostrado como imagem local. */
export default function AuthedImage({ mediaId, alt, className }: Props) {
  const [loaded, setLoaded] = useState<{ id: number; url: string | null } | null>(null)

  useEffect(() => {
    let active = true
    loadMediaUrl(mediaId).then((url) => {
      if (active) setLoaded({ id: mediaId, url })
    })
    return () => {
      active = false
    }
  }, [mediaId])

  const url = loaded?.id === mediaId ? loaded.url : null
  return url ? (
    <img src={url} alt={alt} className={className} />
  ) : (
    <div className={`${className ?? ''} ${styles.imgPlaceholder}`} role="img" aria-label={alt} />
  )
}
