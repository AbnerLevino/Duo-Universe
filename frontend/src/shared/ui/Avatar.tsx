import AuthedImage from './AuthedImage'
import { initialsOf } from '@/shared/lib/names'

type Props = {
  name: string
  mediaId: number | null
  size?: 'sm' | 'md' | 'lg' | 'xl'
}

export default function Avatar({ name, mediaId, size = 'md' }: Props) {
  return mediaId ? (
    <AuthedImage mediaId={mediaId} alt={`Foto de ${name}`} className={`avatar avatar-${size}`} />
  ) : (
    <div className={`avatar avatar-${size}`} role="img" aria-label={`Foto de ${name}`}>
      {initialsOf(name)}
    </div>
  )
}
