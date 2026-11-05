import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError, redeemByLink } from './api'
import type { User } from './session'

/**
 * Quem recebeu um link de convite por fora (WhatsApp etc.) cai direto aqui (P1, Etapa 3).
 * Limitação conhecida: se a pessoa ainda não tiver @handle escolhido, ela é mandada para
 * {@code /vinculo/handle} e, depois de escolher, cai em "/" — o código do convite se perde nesse caso
 * específico (o mesmo acontece, por ora, se ela nem estiver logada: o login com Google também não preserva
 * o destino original). Aceitável para o piloto (casais conhecidos, que já abriram o app antes).
 */
export default function AcceptInviteScreen({ user }: { user: User }) {
  const { code } = useParams<{ code: string }>()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!code) return
    if (!user.handle) {
      navigate('/vinculo/handle', { replace: true })
      return
    }
    let active = true
    redeemByLink(code)
      .then((pairing) => {
        if (active) navigate(`/vinculo/${pairing.id}`, { replace: true })
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof ApiError ? err.message : 'Código inválido ou vencido.')
      })
    return () => {
      active = false
    }
  }, [code, user.handle, navigate])

  if (error) {
    return (
      <main className="screen">
        <h1>Não foi possível aceitar</h1>
        <p role="alert" className="login-error">
          {error}
        </p>
      </main>
    )
  }
  return <main className="screen" aria-busy="true" />
}
