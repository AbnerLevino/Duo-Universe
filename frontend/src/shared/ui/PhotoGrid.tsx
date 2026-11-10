import { useState } from 'react'
import AuthedImage from './AuthedImage'
import { errorMessage } from '@/shared/api/client'
import type { MediaSummary, MediaVisibility } from '@/shared/api/types'
import { deletePhoto, editPhoto } from '@/features/media/api'
import { reportPhoto } from '@/features/social/api'
import { CAPTION_MAX, MEDIA_VISIBILITY_LABELS, MEDIA_VISIBILITY_OPTIONS } from '@/shared/lib/labels'
import { forgetMedia } from '@/shared/session'
import ReportForm from './ReportForm'
import styles from './PhotoGrid.module.css'

type Props = {
  photos: MediaSummary[]
  /** São as fotos da própria pessoa: ela pode editar e apagar; as dos outros só se denunciam. */
  mine: boolean
  /** Avisa a tela que a lista mudou (foto editada ou apagada) para ela recarregar. */
  onChanged: () => void
}

/** A grade de fotos (momentos) e o visualizador que abre ao tocar numa delas. */
export default function PhotoGrid({ photos, mine, onChanged }: Props) {
  const [openId, setOpenId] = useState<number | null>(null)
  const open = photos.find((photo) => photo.id === openId) ?? null

  if (photos.length === 0) return <p className="muted">Nenhum momento ainda.</p>

  return (
    <>
      <ul className={styles.photoGrid}>
        {photos.map((photo) => (
          <li key={photo.id}>
            <button
              type="button"
              className={styles.photoCell}
              onClick={() => setOpenId(photo.id)}
              aria-label={photo.caption ? `Abrir foto: ${photo.caption}` : 'Abrir foto'}
            >
              <AuthedImage
                mediaId={photo.id}
                alt={photo.caption ?? 'Momento'}
                className={styles.photoThumb}
              />
            </button>
          </li>
        ))}
      </ul>
      {open && (
        <PhotoDialog
          photo={open}
          mine={mine}
          onClose={() => setOpenId(null)}
          onChanged={() => {
            setOpenId(null)
            onChanged()
          }}
        />
      )}
    </>
  )
}

type DialogProps = {
  photo: MediaSummary
  mine: boolean
  onClose: () => void
  onChanged: () => void
}

function PhotoDialog({ photo, mine, onClose, onChanged }: DialogProps) {
  const [caption, setCaption] = useState(photo.caption ?? '')
  const [visibility, setVisibility] = useState<MediaVisibility>(photo.visibility)
  const [reporting, setReporting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function save() {
    setBusy(true)
    setError(null)
    try {
      await editPhoto(photo.id, caption, visibility)
      onChanged()
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível salvar.'))
      setBusy(false)
    }
  }

  async function remove() {
    // O app sempre avisa antes de apagar algo que não dá para recuperar.
    if (!window.confirm('Apagar esta foto? Não dá para desfazer.')) return
    setBusy(true)
    setError(null)
    try {
      await deletePhoto(photo.id)
      forgetMedia(photo.id)
      onChanged()
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível apagar.'))
      setBusy(false)
    }
  }

  return (
    <div className={styles.dialogBackdrop}>
      <div className={styles.dialog} role="dialog" aria-modal="true" aria-label="Foto">
        <AuthedImage
          mediaId={photo.id}
          alt={photo.caption ?? 'Momento'}
          className={styles.photoLarge}
        />
        {reporting ? (
          <ReportForm
            send={(reason, details) => reportPhoto(photo.id, reason, details)}
            onClose={() => setReporting(false)}
          />
        ) : mine ? (
          <div className="form">
            <label htmlFor="photo-caption">Legenda</label>
            <input
              id="photo-caption"
              value={caption}
              maxLength={CAPTION_MAX}
              onChange={(e) => setCaption(e.target.value)}
            />
            <label htmlFor="photo-visibility">Quem pode ver</label>
            <select
              id="photo-visibility"
              value={visibility}
              onChange={(e) => setVisibility(e.target.value as MediaVisibility)}
            >
              {MEDIA_VISIBILITY_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {MEDIA_VISIBILITY_LABELS[option]}
                </option>
              ))}
            </select>
            {error && (
              <p role="alert" className="login-error">
                {error}
              </p>
            )}
            <button type="button" className="button primary" onClick={save} disabled={busy}>
              Salvar
            </button>
            <button type="button" className="button" onClick={remove} disabled={busy}>
              Apagar foto
            </button>
          </div>
        ) : (
          <div className="form">
            {photo.caption && <p>{photo.caption}</p>}
            <button type="button" className="button subtle" onClick={() => setReporting(true)}>
              Denunciar foto
            </button>
          </div>
        )}
        <button type="button" className="button subtle" onClick={onClose}>
          Fechar
        </button>
      </div>
    </div>
  )
}
