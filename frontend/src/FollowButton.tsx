import { useState } from 'react'
import { errorMessage, follow, type Relation, unfollow } from './api'

type Props = {
  handle: string
  relation: Relation
  onChange: (relation: Relation) => void
}

const LABELS: Record<Relation, string> = {
  NONE: 'Seguir',
  REQUESTED: 'Pedido enviado (cancelar)',
  FOLLOWING: 'Seguindo',
}

/** Seguir, cancelar o pedido ou deixar de seguir: o mesmo botão em qualquer tela, com a regra num lugar só. */
export default function FollowButton({ handle, relation, onChange }: Props) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function toggle() {
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      if (relation === 'NONE') onChange((await follow(handle)).relation)
      else {
        await unfollow(handle)
        onChange('NONE')
      }
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível concluir.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <button
        type="button"
        className={`button ${relation === 'NONE' ? 'primary' : ''}`}
        onClick={toggle}
        disabled={busy}
      >
        {LABELS[relation]}
      </button>
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
    </>
  )
}
