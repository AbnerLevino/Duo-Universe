import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage } from '@/shared/api/client'
import type { MediaVisibility } from '@/shared/api/types'
import { uploadPhoto } from './api'
import { preparePhoto } from '@/shared/lib/imageTools'
import { CAPTION_MAX, MEDIA_VISIBILITY_LABELS, MEDIA_VISIBILITY_OPTIONS } from '@/shared/lib/labels'

/** Guardar um momento (foto) no Personal Universe. Por padrão só a própria pessoa vê. */
export default function NewMomentScreen() {
  const navigate = useNavigate()
  const [file, setFile] = useState<File | null>(null)
  const [caption, setCaption] = useState('')
  const [visibility, setVisibility] = useState<MediaVisibility>('ONLY_ME')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!file || busy) return
    setBusy(true)
    setError(null)
    try {
      await uploadPhoto(await preparePhoto(file), caption, visibility)
      navigate('/inicio', { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível guardar o momento.'))
      setBusy(false)
    }
  }

  return (
    <main className="screen">
      <h1>Novo momento</h1>
      <form onSubmit={submit} className="form">
        <label htmlFor="photo">Foto</label>
        <input
          id="photo"
          type="file"
          accept="image/*"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        <label htmlFor="caption">
          Legenda ({caption.length}/{CAPTION_MAX})
        </label>
        <input
          id="caption"
          value={caption}
          maxLength={CAPTION_MAX}
          onChange={(e) => setCaption(e.target.value)}
        />
        <label htmlFor="visibility">Quem pode ver</label>
        <select
          id="visibility"
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
        <button type="submit" className="button primary" disabled={!file || busy}>
          {busy ? 'Guardando…' : 'Guardar momento'}
        </button>
        <Link to="/inicio" className="button subtle">
          Cancelar
        </Link>
      </form>
    </main>
  )
}
