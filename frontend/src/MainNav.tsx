import { NavLink } from 'react-router-dom'

const ITEMS = [
  { to: '/inicio', label: 'Início' },
  { to: '/buscar', label: 'Buscar' },
  { to: '/vinculo/convidar', label: 'Vínculo' },
  { to: '/perfil/editar', label: 'Perfil' },
]

/** O cabeçalho do app: a navegação principal, fixa no topo da tela (mobile-first, DEC-013). */
export default function MainNav() {
  return (
    <nav className="main-nav" aria-label="Navegação principal">
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
