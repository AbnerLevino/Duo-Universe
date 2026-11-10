import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '@/shared/api/client'
import type { Pairing } from '@/shared/api/types'
import { cancelPairing, confirmPairing, getPairing } from './api'
import type { User } from '@/shared/session'

/**
 * A tela de "firmar" (V5, RN-08). Ordem decidida em 2026-11-05: o vínculo firma primeiro — história e datas
 * só vêm depois, já dentro do Couple Universe. Sem tempo real ainda (isso é o Bloco D, mais pra frente): se a
 * outra pessoa ainda não confirmou, a pessoa atualiza manualmente.
 */
export default function ConfirmScreen({ user }: { user: User }) {
  const { pairingId } = useParams<{ pairingId: string }>()
  const navigate = useNavigate()
  const id = Number(pairingId)
  const [pairing, setPairing] = useState<Pairing | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    getPairing(id)
      .then(setPairing)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : 'Pareamento não encontrado.'),
      )
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    if (pairing?.state === 'CONFIRMED') navigate('/casal', { replace: true })
    if (pairing?.state === 'CANCELLED') navigate('/vinculo/convidar', { replace: true })
  }, [pairing, navigate])

  async function firmar() {
    setBusy(true)
    setError(null)
    try {
      setPairing(await confirmPairing(id))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível firmar.')
    } finally {
      setBusy(false)
    }
  }

  async function desistir() {
    setBusy(true)
    setError(null)
    try {
      await cancelPairing(id)
      navigate('/vinculo/convidar', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível desistir.')
      setBusy(false)
    }
  }

  if (error) {
    return (
      <main className="screen">
        <h1>Não encontramos esse vínculo</h1>
        <p role="alert" className="login-error">
          {error}
        </p>
      </main>
    )
  }
  if (!pairing) return <main className="screen" aria-busy="true" />

  const iAmInitiator = pairing.initiatorUserId === user.id
  const myConfirmedAt = iAmInitiator ? pairing.initiatorConfirmedAt : pairing.inviteeConfirmedAt
  const partnerConfirmedAt = iAmInitiator
    ? pairing.inviteeConfirmedAt
    : pairing.initiatorConfirmedAt
  const waitingForInvitee = pairing.inviteeUserId === null

  return (
    <main className="screen">
      <h1>Firmar o vínculo</h1>
      {waitingForInvitee && <p className="muted">Aguardando alguém usar o link que você enviou.</p>}
      {!waitingForInvitee && myConfirmedAt && !partnerConfirmedAt && (
        <p className="muted">Você já confirmou. Esperando a outra pessoa confirmar também.</p>
      )}
      {!waitingForInvitee && !myConfirmedAt && (
        <>
          <p className="muted">Confirme para dar início ao Couple Universe de vocês dois.</p>
          <button type="button" className="button primary" disabled={busy} onClick={firmar}>
            Firmar
          </button>
        </>
      )}
      {!waitingForInvitee && myConfirmedAt && !partnerConfirmedAt && (
        <button type="button" className="button subtle" onClick={load}>
          Atualizar
        </button>
      )}
      <button type="button" className="button subtle" disabled={busy} onClick={desistir}>
        Desistir deste convite
      </button>
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
    </main>
  )
}
