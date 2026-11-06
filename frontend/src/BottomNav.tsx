import { NavLink } from 'react-router-dom'

const ITEMS = [
  { to: '/inicio', label: 'Início' },
  { to: '/buscar', label: 'Buscar' },
  { to: '/vinculo/convidar', label: 'Vínculo' },
  { to: '/perfil/editar', label: 'Perfil' },
]

/** A navegação principal do app, fixa embaixo da tela (mobile-first, DEC-013). */
export default function BottomNav() {
  return (
    <nav className="bottom-nav" aria-label="Navegação principal">
      {ITEMS.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
