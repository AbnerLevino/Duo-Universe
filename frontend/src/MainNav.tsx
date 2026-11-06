import { Heart, House, Search, User } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const LINKS = [
  { to: '/inicio', label: 'Início', icon: House },
  { to: '/buscar', label: 'Buscar', icon: Search },
  { to: '/vinculo/convidar', label: 'Vínculo', icon: Heart },
]

type Props = {
  /** "Perfil" não troca de página: abre o modal de editar perfil. */
  onProfile: () => void
}

/**
 * A navegação principal (DEC-013, mobile-first). No computador é a lista do painel à esquerda; no celular é uma
 * faixa fixa embaixo que se arrasta com o dedo para os lados. Os ícones são só enfeite: o texto continua ao lado,
 * então os leitores de tela não os repetem (`aria-hidden`).
 */
export default function MainNav({ onProfile }: Props) {
  return (
    <nav className="main-nav" aria-label="Navegação principal">
      {LINKS.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
        >
          <Icon aria-hidden size={20} />
          <span>{label}</span>
        </NavLink>
      ))}
      <button type="button" className="nav-item" onClick={onProfile}>
        <User aria-hidden size={20} />
        <span>Perfil</span>
      </button>
    </nav>
  )
}
