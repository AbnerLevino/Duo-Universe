import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '@/shared/api/client'
import type { Couple, MediaSummary, Pairing } from '@/shared/api/types'
import { myCouple, myPairings } from '@/features/relationships/api'
import { photosOf } from '@/features/media/api'
import { MARITAL_LABELS } from '@/shared/lib/labels'
import { useMe } from '@/shared/context/meContext'
import PhotoGrid from '@/shared/ui/PhotoGrid'
import styles from './HomeScreen.module.css'

type Data = {
  photos: MediaSummary[]
  pairings: Pairing[]
  couple: Couple | null
}

/**
 * A página inicial do Personal Universe (Épico 3): os momentos da pessoa e o caminho para o vínculo. A foto, os
 * números de Duollowers e o botão Editar moraram no painel (`SideProfile`); o nome, o DuoName, a profissão e a bio
 * ficam aqui por enquanto, até decidirmos onde mostrá-los. É o "seu universo" — não um feed de outras pessoas.
 */
export default function HomeScreen() {
  const { profile, requests } = useMe()
  const [data, setData] = useState<Data | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [version, setVersion] = useState(0)
  const profileId = profile?.id

  useEffect(() => {
    if (profileId === undefined) return
    let active = true
    Promise.all([photosOf(profileId), myPairings(), myCouple()])
      .then(([photos, pairings, couple]) => {
        if (active) setData({ photos, pairings, couple })
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Não foi possível carregar o seu universo.'))
      })
    return () => {
      active = false
    }
  }, [profileId, version])

  if (error)
    return (
      <main className="screen">
        <p role="alert" className="login-error">
          {error}
        </p>
      </main>
    )
  if (!profile || !data) return <main className="screen" aria-busy="true" />

  const { photos, pairings, couple } = data
  const subtitle = [profile.profession, MARITAL_LABELS[profile.maritalStatus]]
    .filter(Boolean)
    .join(' · ')

  return (
    <main className="screen">
      <div className={styles.profileInfo}>
        <h1>{profile.displayName}</h1>
        <p className="muted">@{profile.handle}</p>
        <p className="muted">{subtitle}</p>
      </div>
      {profile.bio && <p>{profile.bio}</p>}
      {requests > 0 && (
        <Link to="/lista/pedidos" className="badge">
          {requests} {requests === 1 ? 'pedido' : 'pedidos'} para seguir
        </Link>
      )}

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
    </main>
  )
}
