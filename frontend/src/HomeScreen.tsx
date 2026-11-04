import type { User } from './session'

type Props = {
  user: User
  onLogout: () => void
  onLogoutAll: () => void
}

// Tela inicial provisória: o Couple Universe (vínculo, contador, fotos) vem nas próximas semanas.
export default function HomeScreen({ user, onLogout, onLogoutAll }: Props) {
  return (
    <main className="screen home">
      <header className="profile">
        <div className="avatar" aria-hidden="true">
          {user.displayName.charAt(0).toUpperCase()}
        </div>
        <div>
          <h1>{user.displayName}</h1>
          <p className="muted">{user.email}</p>
        </div>
      </header>
      <h2>Bem-vindo ao Duo Universe!</h2>
      <p className="muted">
        Em breve você poderá se vincular à pessoa que faz parte do seu universo.
      </p>
      <div className="actions">
        <button type="button" className="button" onClick={onLogout}>
          Sair
        </button>
        <button type="button" className="button subtle" onClick={onLogoutAll}>
          Sair de todos os aparelhos
        </button>
      </div>
    </main>
  )
}
