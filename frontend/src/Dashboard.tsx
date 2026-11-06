import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { myCouple } from './api'
import type { User } from './session'

/**
 * A raiz do app, depois do login: decide pra onde mandar a pessoa, sem ela saber nenhum id de antemão —
 * DuoName faltando → escolher; casal ativo → o universo do casal; senão → o Personal Universe (Épico 3).
 * Quem está solteiro nunca cai mais direto num formulário de convite: o vínculo é um caminho dentro da home.
 */
export default function Dashboard({ user }: { user: User }) {
  const navigate = useNavigate()

  useEffect(() => {
    if (!user.handle) {
      navigate('/vinculo/handle', { replace: true })
      return
    }
    let active = true
    myCouple().then((couple) => {
      if (!active) return
      navigate(couple ? '/casal' : '/inicio', { replace: true })
    })
    return () => {
      active = false
    }
  }, [user, navigate])

  return <main className="screen" aria-busy="true" />
}
