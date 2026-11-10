import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Avatar from '@/shared/ui/Avatar'
import { errorMessage } from '@/shared/api/client'
import type { MediaSummary } from '@/shared/api/types'
import { getProfile, type PublicProfile } from './api'
import { photosOf } from '@/features/media/api'
import { blockUser, reportProfile } from '@/features/social/api'
import FollowButton from '@/shared/ui/FollowButton'
import { MARITAL_LABELS, TERMS } from '@/shared/lib/labels'
import PhotoGrid from '@/shared/ui/PhotoGrid'
import ReportForm from '@/shared/ui/ReportForm'

type Loaded =
  | { handle: string; profile: PublicProfile; photos: MediaSummary[] }
  | { handle: string; error: string }

/** O perfil de outra pessoa, como ela permite que você veja (regra central de acesso do backend). */
export default function PublicProfileScreen() {
  const { handle = '' } = useParams()
  const navigate = useNavigate()
  const [loaded, setLoaded] = useState<Loaded | null>(null)
  const [version, setVersion] = useState(0)
  const [reporting, setReporting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    getProfile(handle)
      .then(async (profile) => {
        if (profile.self) {
          navigate('/inicio', { replace: true })
          return
        }
        const photos = profile.canSeeContent ? await photosOf(profile.userId) : []
        if (active) setLoaded({ handle, profile, photos })
      })
      .catch((err) => {
        if (active) setLoaded({ handle, error: errorMessage(err, 'Perfil não encontrado.') })
      })
    return () => {
      active = false
    }
  }, [handle, navigate, version])

  const current = loaded?.handle === handle ? loaded : null

  async function block() {
    if (!window.confirm(`Bloquear @${handle}? Vocês deixam de se ver e de se seguir.`)) return
    setActionError(null)
    try {
      await blockUser(handle)
      navigate('/buscar', { replace: true })
    } catch (err) {
      setActionError(errorMessage(err, 'Não foi possível bloquear.'))
    }
  }

  if (!current) return <main className="screen" aria-busy="true"></main>
  if ('error' in current)
    return (
      <main className="screen">
        <p role="alert" className="login-error">
          {current.error}
        </p>
        <Link to="/buscar" className="button">
          Voltar para a busca
        </Link>
      </main>
    )

  const { profile, photos } = current
  const subtitle = [profile.profession, MARITAL_LABELS[profile.maritalStatus]]
    .filter(Boolean)
    .join(' · ')

  return (
    <main className="screen">
      <header className="profile-header">
        <Avatar name={profile.displayName} mediaId={profile.avatarMediaId} size="lg" />
        <div>
          <h1>{profile.displayName}</h1>
          <p className="muted">@{profile.handle}</p>
          <p className="muted">{subtitle}</p>
        </div>
      </header>
      {profile.bio && <p>{profile.bio}</p>}
      <ul className="stats">
        <li>
          <strong>{profile.duollowers}</strong> {TERMS.followers}
        </li>
        <li>
          <strong>{profile.duollowing}</strong> {TERMS.following}
        </li>
      </ul>
      <FollowButton
        handle={profile.handle}
        relation={profile.relation}
        onChange={() => setVersion((v) => v + 1)}
      />

      <section>
        <h2>Momentos</h2>
        {profile.canSeeContent ? (
          <PhotoGrid photos={photos} mine={false} onChanged={() => setVersion((v) => v + 1)} />
        ) : (
          <p className="muted">
            {profile.relation === 'REQUESTED'
              ? 'Pedido enviado. Os momentos aparecem quando ele for aceito.'
              : `Este perfil é privado. Siga para pedir acesso aos momentos.`}
          </p>
        )}
      </section>

      <section>
        {reporting ? (
          <ReportForm
            send={(reason, details) => reportProfile(profile.handle, reason, details)}
            onClose={() => setReporting(false)}
          />
        ) : (
          <div className="actions">
            <button type="button" className="button subtle" onClick={() => setReporting(true)}>
              Denunciar perfil
            </button>
            <button type="button" className="button subtle" onClick={block}>
              Bloquear
            </button>
          </div>
        )}
        {actionError && (
          <p role="alert" className="login-error">
            {actionError}
          </p>
        )}
      </section>
    </main>
  )
}
