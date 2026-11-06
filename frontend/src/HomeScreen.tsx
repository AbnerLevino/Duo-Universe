import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import Avatar from './Avatar'
import BottomNav from './BottomNav'
import {
  type Couple,
  errorMessage,
  followRequests,
  getProfile,
  type MediaSummary,
  myCouple,
  myPairings,
  myProfile,
  type MyProfile,
  type Pairing,
  photosOf,
  type PublicProfile,
} from './api'
import { MARITAL_LABELS, TERMS } from './labels'
import PhotoGrid from './PhotoGrid'
import type { User } from './session'

type Data = {
  profile: MyProfile
  counts: PublicProfile
  photos: MediaSummary[]
  requests: number
  pairings: Pairing[]
  couple: Couple | null
}

/**
 * A página inicial do Personal Universe (Épico 3): o cartão da própria pessoa, os números de Duollowers, os
 * momentos dela e o caminho para o vínculo. É o "seu universo" — não um feed de outras pessoas.
 */
export default function HomeScreen({ user }: { user: User }) {
  const [data, setData] = useState<Data | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let active = true
    myProfile()
      .then(async (profile) => {
        if (!profile.handle) return null
        const [counts, photos, requests, pairings, couple] = await Promise.all([
          getProfile(profile.handle),
          photosOf(profile.id),
          followRequests(),
          myPairings(),
          myCouple(),
        ])
        return { profile, counts, photos, requests: requests.length, pairings, couple }
      })
      .then((loaded) => {
        if (active && loaded) setData(loaded)
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Não foi possível carregar o seu universo.'))
      })
    return () => {
      active = false
    }
  }, [user.id, version])

  if (!user.handle) return <Navigate to="/vinculo/handle" replace />
  if (error)
    return (
      <main className="screen with-nav">
        <p role="alert" className="login-error">
          {error}
        </p>
        <BottomNav />
      </main>
    )
  if (!data) return <main className="screen with-nav" aria-busy="true" />

  const { profile, counts, photos, requests, pairings, couple } = data
  const subtitle = [profile.profession, MARITAL_LABELS[profile.maritalStatus]]
    .filter(Boolean)
    .join(' · ')

  return (
    <main className="screen with-nav">
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
          <Link to="/lista/duollowers">
            <strong>{counts.duollowers}</strong> {TERMS.followers}
          </Link>
        </li>
        <li>
          <Link to="/lista/duollowing">
            <strong>{counts.duollowing}</strong> {TERMS.following}
          </Link>
        </li>
        {requests > 0 && (
          <li>
            <Link to="/lista/pedidos" className="badge">
              {requests} {requests === 1 ? 'pedido' : 'pedidos'} para seguir
            </Link>
          </li>
        )}
      </ul>
      <Link to="/perfil/editar" className="button">
        Editar perfil
      </Link>

      <section>
        <h2>Meus momentos</h2>
        <Link to="/momentos/novo" className="button primary">
          Novo momento
        </Link>
        <PhotoGrid photos={photos} mine onChanged={() => setVersion((v) => v + 1)} />
      </section>

      <section>
        <h2>Vínculo</h2>
        {couple ? (
          <Link to="/casal" className="button">
            Ir para o nosso universo
          </Link>
        ) : pairings.length > 0 ? (
          <Link to={`/vinculo/${pairings[0].id}`} className="button">
            Você tem um convite de vínculo
          </Link>
        ) : (
          <Link to="/vinculo/convidar" className="button">
            Vincular-se
          </Link>
        )}
      </section>
      <BottomNav />
    </main>
  )
}
