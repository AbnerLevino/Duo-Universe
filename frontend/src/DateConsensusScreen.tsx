import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { acceptDates, ApiError, getPairing, proposeDates, type Pairing } from './api'
import type { User } from './session'

/** RN-27/RN-28: um propõe as duas datas do casal, o outro aceita ou propõe outras, até concordarem. */
export default function DateConsensusScreen({ user }: { user: User }) {
  const { pairingId } = useParams<{ pairingId: string }>()
  const navigate = useNavigate()
  const id = Number(pairingId)
  const [pairing, setPairing] = useState<Pairing | null>(null)
  const [knownSince, setKnownSince] = useState('')
  const [datingSince, setDatingSince] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    getPairing(id)
      .then(setPairing)
      .catch((err: unknown) => setError(err instanceof ApiError ? err.message : 'Não encontrado.'))
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  async function submitProposal(event: FormEvent) {
    event.preventDefault()
    if (!knownSince || !datingSince || busy) return
    setBusy(true)
    setError(null)
    try {
      await proposeDates(id, knownSince, datingSince)
      load()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível propor as datas.')
    } finally {
      setBusy(false)
    }
  }

  async function accept() {
    setBusy(true)
    setError(null)
    try {
      await acceptDates(id)
      load()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível aceitar.')
    } finally {
      setBusy(false)
    }
  }

  if (error && !pairing) {
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

  const iProposed = pairing.dateProposedByUserId === user.id
  const hasProposal = pairing.dateProposedByUserId !== null
  const accepted = pairing.datesAcceptedAt !== null

  return (
    <main className="screen">
      <h1>Datas do casal</h1>
      {accepted ? (
        <>
          <p className="muted">
            Se conheceram em {formatDate(pairing.proposedKnownSinceDate)} e começaram a namorar em{' '}
            {formatDate(pairing.proposedDatingSinceDate)}.
          </p>
        </>
      ) : (
        <>
          {hasProposal && (
            <p className="muted">
              {iProposed ? 'Você propôs: ' : 'Proposta recebida: '}
              se conheceram em {formatDate(pairing.proposedKnownSinceDate)}, começaram a namorar em{' '}
              {formatDate(pairing.proposedDatingSinceDate)}.
            </p>
          )}
          {hasProposal && !iProposed && (
            <button type="button" className="button primary" disabled={busy} onClick={accept}>
              Aceitar essas datas
            </button>
          )}
          <form onSubmit={submitProposal} className="form">
            <label htmlFor="known-since">
              {hasProposal ? 'Ou proponha outras datas' : 'Quando vocês se conheceram?'}
            </label>
            <input
              id="known-since"
              type="date"
              value={knownSince}
              onChange={(e) => setKnownSince(e.target.value)}
            />
            <label htmlFor="dating-since">Quando começaram a namorar?</label>
            <input
              id="dating-since"
              type="date"
              value={datingSince}
              onChange={(e) => setDatingSince(e.target.value)}
            />
            <button type="submit" className="button" disabled={busy || !knownSince || !datingSince}>
              {hasProposal ? 'Propor estas datas' : 'Propor'}
            </button>
          </form>
        </>
      )}
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
      <button type="button" className="button subtle" onClick={() => navigate('/casal')}>
        Voltar
      </button>
    </main>
  )
}

function formatDate(value: string | null): string {
  if (!value) return '—'
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR')
}
