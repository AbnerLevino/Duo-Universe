import { type FormEvent, useState } from 'react'
import { errorMessage } from '@/shared/api/client'
import type { ReportReason } from '@/shared/api/types'
import { REPORT_REASONS } from '@/shared/lib/labels'

type Props = {
  send: (reason: ReportReason, details: string) => Promise<unknown>
  onClose: () => void
}

const DETAILS_MAX = 300

/** O formulário de denúncia (seção 16): motivo fixo e, para "Outro", um texto explicando. */
export default function ReportForm({ send, onClose }: Props) {
  const [reason, setReason] = useState<ReportReason>('SPAM')
  const [details, setDetails] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [done, setDone] = useState(false)
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      await send(reason, details)
      setDone(true)
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível enviar a denúncia.'))
    } finally {
      setBusy(false)
    }
  }

  if (done)
    return (
      <div className="form">
        <p role="status">Denúncia enviada. Obrigado por avisar.</p>
        <button type="button" className="button" onClick={onClose}>
          Fechar
        </button>
      </div>
    )

  return (
    <form onSubmit={submit} className="form">
      <label htmlFor="report-reason">Motivo</label>
      <select
        id="report-reason"
        value={reason}
        onChange={(e) => setReason(e.target.value as ReportReason)}
      >
        {REPORT_REASONS.map((item) => (
          <option key={item.value} value={item.value}>
            {item.label}
          </option>
        ))}
      </select>
      <label htmlFor="report-details">
        Detalhes {reason === 'OTHER' ? '(obrigatório)' : '(opcional)'}
      </label>
      <textarea
        id="report-details"
        value={details}
        maxLength={DETAILS_MAX}
        onChange={(e) => setDetails(e.target.value)}
        rows={3}
      />
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
      <button type="submit" className="button primary" disabled={busy}>
        Enviar denúncia
      </button>
      <button type="button" className="button subtle" onClick={onClose}>
        Cancelar
      </button>
    </form>
  )
}
