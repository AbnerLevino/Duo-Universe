import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError, leaveCouple, myCouple, myPairings, pauseCouple, resumeCouple, type Couple } from './api'
import type { User } from './session'

type Props = {
  user: User
  onLogout: () => void
  onLogoutAll: () => void
}

const STATE_LABELS: Record<Couple['state'], string> = {
  ACTIVE: 'ativo',
  PAUSED: 'em pausa',
  ENDED: 'encerrado',
}

/** O Couple Universe depois de nascido (Etapa 8): status, atalhos para história/datas, pausar/sair (RN-13 a 18). */
export default function CoupleHomeScreen({ user, onLogout, onLogoutAll }: Props) {
  const navigate = useNavigate()
  const [couple, setCouple] = useState<Couple | null>(null)
  const [pairingId, setPairingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([myCouple(), myPairings()])
      .then(([c, pairings]) => {
        if (!active) return
        if (!c) {
          navigate('/vinculo/convidar', { replace: true })
          return
        }
        setCouple(c)
        setPairingId(pairings[0]?.id ?? null)
      })
      .catch((err: unknown) => setError(err instanceof ApiError ? err.message : 'Não foi possível carregar.'))
    return () => {
      active = false
    }
  }, [navigate])

  async function togglePause() {
    if (!couple) return
    setBusy(true)
    setError(null)
    try {
      setCouple(couple.state === 'PAUSED' ? await resumeCouple(couple.id) : await pauseCouple(couple.id))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível.')
    } finally {
      setBusy(false)
    }
  }

  async function sair() {
    if (!couple) return
    if (!window.confirm('Tem certeza? Isso termina o vínculo para os dois (RN-17: nada é apagado).')) return
    setBusy(true)
    setError(null)
    try {
      await leaveCouple(couple.id)
      navigate('/vinculo/convidar', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível sair.')
      setBusy(false)
    }
  }

  if (!couple) return <main className="screen" aria-busy="true" />

  return (
    <main className="screen home">
      <header className="profile">
        <div className="avatar" aria-hidden="true">
          {user.displayName.charAt(0).toUpperCase()}
        </div>
        <div>
          <h1>{user.displayName}</h1>
          <p className="muted">{user.email}</p>
        </div>
      </header>
      <h2>Couple Universe</h2>
      <p className="muted">Vínculo {STATE_LABELS[couple.state]}.</p>
      {couple.datingSinceDate ? (
        <p className="muted">
          Juntos desde {new Date(`${couple.datingSinceDate}T00:00:00`).toLocaleDateString('pt-BR')}.
        </p>
      ) : (
        pairingId && (
          <button type="button" className="button" onClick={() => navigate(`/vinculo/${pairingId}/datas`)}>
            Combinar as datas do casal
          </button>
        )
      )}
      {pairingId && (
        <button type="button" className="button" onClick={() => navigate(`/vinculo/${pairingId}/historia`)}>
          Escrever/ler a história
        </button>
      )}
      <div className="actions">
        {couple.state !== 'ENDED' && (
          <button type="button" className="button subtle" disabled={busy} onClick={togglePause}>
            {couple.state === 'PAUSED' ? 'Retomar' : 'Pausar'}
          </button>
        )}
        <button type="button" className="button subtle" disabled={busy} onClick={sair}>
          Sair do vínculo
        </button>
        <button type="button" className="button" onClick={onLogout}>
          Sair
        </button>
        <button type="button" className="button subtle" onClick={onLogoutAll}>
          Sair de todos os aparelhos
        </button>
      </div>
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
    </main>
  )
}
