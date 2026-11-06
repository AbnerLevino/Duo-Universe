import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError, chooseHandle } from './api'
import type { User } from './session'

type Props = {
  onChosen: (user: User) => void
}

// Mesmo formato que o backend exige (RN-30): 3 a 20 letras, números ou "_".
const HANDLE_FORMAT = /^[A-Za-z0-9_]{3,20}$/

// Tela obrigatória no próximo login para quem ainda não escolheu um @handle (Etapa 4) — é por ele que a sua
// pessoa vai te encontrar para o convite do vínculo.
export default function HandleSetupScreen({ onChosen }: Props) {
  const navigate = useNavigate()
  const [handle, setHandle] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const valid = HANDLE_FORMAT.test(handle)

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!valid || saving) return
    setSaving(true)
    setError(null)
    try {
      const user = await chooseHandle(handle)
      onChosen(user)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível salvar o @handle.')
      setSaving(false)
    }
  }

  return (
    <main className="screen">
      <h1>Escolha seu @handle</h1>
      <p className="muted">
        É assim que a sua pessoa vai te encontrar para o vínculo. 3 a 20 letras, números ou
        &quot;_&quot;.
      </p>
      <form onSubmit={submit} className="form">
        <label htmlFor="handle">@handle</label>
        <input
          id="handle"
          value={handle}
          onChange={(e) => setHandle(e.target.value)}
          placeholder="seu_handle"
          autoFocus
        />
        {error && (
          <p role="alert" className="login-error">
            {error}
          </p>
        )}
        <button type="submit" className="button primary" disabled={!valid || saving}>
          {saving ? 'Salvando…' : 'Continuar'}
        </button>
      </form>
    </main>
  )
}
