import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Avatar from './Avatar'
import {
  acceptRequest,
  blockedUsers,
  duollowers,
  duollowing,
  errorMessage,
  followRequests,
  type ProfileCard,
  rejectRequest,
  removeDuollower,
  unblockUser,
  unfollow,
} from './api'
import { TERMS } from './labels'

type Action = { label: string; run: (handle: string) => Promise<unknown> }
type Kind = { title: string; empty: string; load: () => Promise<ProfileCard[]>; actions: Action[] }

// Uma tela só para as quatro listas: o que muda de uma para outra é a fonte dos dados e as ações de cada linha.
const KINDS: Record<string, Kind> = {
  duollowers: {
    title: TERMS.followers,
    empty: 'Ninguém te segue ainda.',
    load: duollowers,
    actions: [{ label: 'Remover', run: removeDuollower }],
  },
  duollowing: {
    title: TERMS.following,
    empty: 'Você ainda não segue ninguém.',
    load: duollowing,
    actions: [{ label: 'Deixar de seguir', run: unfollow }],
  },
  pedidos: {
    title: 'Pedidos para seguir',
    empty: 'Nenhum pedido no momento.',
    load: followRequests,
    actions: [
      { label: 'Aceitar', run: acceptRequest },
      { label: 'Recusar', run: rejectRequest },
    ],
  },
  bloqueados: {
    title: 'Bloqueados',
    empty: 'Você não bloqueou ninguém.',
    load: blockedUsers,
    actions: [{ label: 'Desbloquear', run: unblockUser }],
  },
}

export default function FollowListScreen() {
  const { kind = '' } = useParams()
  const config = KINDS[kind]
  const [loaded, setLoaded] = useState<{ kind: string; cards: ProfileCard[] } | null>(null)
  const [version, setVersion] = useState(0)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!config) return
    let active = true
    config
      .load()
      .then((cards) => {
        if (active) setLoaded({ kind, cards })
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Não foi possível carregar a lista.'))
      })
    return () => {
      active = false
    }
  }, [kind, config, version])

  async function act(action: Action, handle: string) {
    setError(null)
    try {
      await action.run(handle)
      setVersion((v) => v + 1)
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível concluir.'))
    }
  }

  if (!config)
    return (
      <main className="screen">
        <p role="alert" className="login-error">
          Lista não encontrada.
        </p>
      </main>
    )

  const cards = loaded?.kind === kind ? loaded.cards : null

  return (
    <main className="screen">
      <Link to="/inicio" className="button subtle">
        Voltar
      </Link>
      <h1>{config.title}</h1>
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
      {!cards && (
        <p aria-busy="true" className="muted">
          Carregando…
        </p>
      )}
      {cards && cards.length === 0 && <p className="muted">{config.empty}</p>}
      {cards && (
        <ul className="list">
          {cards.map((card) => (
            <li key={card.userId} className="list-item">
              <Link to={`/u/${card.handle}`} className="list-main">
                <Avatar name={card.displayName} mediaId={card.avatarMediaId} size="sm" />
                <span>
                  <strong>{card.displayName}</strong>
                  <span className="muted"> @{card.handle}</span>
                </span>
              </Link>
              <div className="list-actions">
                {config.actions.map((action) => (
                  <button
                    key={action.label}
                    type="button"
                    className="button"
                    onClick={() => act(action, card.handle)}
                  >
                    {action.label}
                  </button>
                ))}
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
