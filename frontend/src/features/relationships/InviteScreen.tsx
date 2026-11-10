import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '@/shared/api/client'
import { inviteByHandle, inviteByLink, type IssuedInvite } from './api'

// V1 do fluxo do vínculo (docs/epico-2-vinculo.md): A busca B pelo @handle (padrão) ou gera um link pra
// mandar por fora, quando ainda não sabe o @handle dela.
export default function InviteScreen() {
  const navigate = useNavigate()
  const [handle, setHandle] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [sending, setSending] = useState(false)
  const [link, setLink] = useState<IssuedInvite | null>(null)

  async function sendByHandle(event: FormEvent) {
    event.preventDefault()
    if (!handle || sending) return
    setSending(true)
    setError(null)
    try {
      const pairing = await inviteByHandle(handle.replace(/^@/, ''))
      navigate(`/vinculo/${pairing.id}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível convidar essa pessoa.')
      setSending(false)
    }
  }

  async function generateLink() {
    setSending(true)
    setError(null)
    try {
      setLink(await inviteByLink())
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível gerar o link.')
    } finally {
      setSending(false)
    }
  }

  const linkUrl = link ? `${window.location.origin}/convite/${link.code}` : null

  return (
    <main className="screen">
      <h1>Chame a sua pessoa</h1>
      <form onSubmit={sendByHandle} className="form">
        <label htmlFor="handle">@handle da pessoa</label>
        <input
          id="handle"
          value={handle}
          onChange={(e) => setHandle(e.target.value)}
          placeholder="@handle_dela"
        />
        <button type="submit" className="button primary" disabled={!handle || sending}>
          Convidar
        </button>
      </form>
      <p className="muted">
        Ainda não sabe o @handle dela? Gere um link pra mandar por fora (válido por 7 dias):
      </p>
      <button type="button" className="button subtle" onClick={generateLink} disabled={sending}>
        Gerar link de convite
      </button>
      {linkUrl && link && (
        <div className="form">
          <p className="muted">
            Envie este link: <code>{linkUrl}</code>
          </p>
          <button
            type="button"
            className="button"
            onClick={() => navigate(`/vinculo/${link.pairingId}`)}
          >
            Já enviei, ver status
          </button>
        </div>
      )}
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
    </main>
  )
}
