import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { myCouple, myPairings } from './api'
import type { User } from './session'

/**
 * A raiz do app, depois do login: decide pra onde mandar a pessoa, sem ela saber nenhum id de antemão —
 * handle faltando → casal ativo → convite pendente → convidar alguém, nesta ordem (Etapa 13/14).
 */
export default function Dashboard({ user }: { user: User }) {
  const navigate = useNavigate()

  useEffect(() => {
    if (!user.handle) {
      navigate('/vinculo/handle', { replace: true })
      return
    }
    let active = true
    Promise.all([myCouple(), myPairings()]).then(([couple, pairings]) => {
      if (!active) return
      if (couple) navigate('/casal', { replace: true })
      else if (pairings.length > 0) navigate(`/vinculo/${pairings[0].id}`, { replace: true })
      else navigate('/vinculo/convidar', { replace: true })
    })
    return () => {
      active = false
    }
  }, [user, navigate])

  return <main className="screen" aria-busy="true" />
}
