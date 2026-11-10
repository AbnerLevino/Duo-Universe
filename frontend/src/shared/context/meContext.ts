import { createContext, useContext } from 'react'
import type { MyProfile } from '../api/types'

/**
 * O "eu" que o layout carrega uma vez só e reparte com as telas: o perfil, os números do painel e a quantidade de
 * pedidos para seguir. Sem isso, cada tela buscaria os mesmos dados por conta própria.
 */
export type Me = {
  /** `null` enquanto carrega (ou se a busca falhou). */
  profile: MyProfile | null
  duollowers: number
  duollowing: number
  requests: number
  /** Recarrega tudo depois de algo mudar (foto, nome, DuoName...). */
  reload: () => void
  /** Abre o modal de editar perfil. */
  openEdit: () => void
}

export const MeContext = createContext<Me | null>(null)

export function useMe(): Me {
  const me = useContext(MeContext)
  if (!me) throw new Error('useMe só funciona dentro do AppLayout')
  return me
}
