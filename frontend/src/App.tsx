import { useEffect, useState } from 'react'
import HomeScreen from './HomeScreen'
import LoginScreen from './LoginScreen'
import { getCurrentUser, logout, logoutAll, type User } from './session'

type State =
  { status: 'loading' } | { status: 'anonymous' } | { status: 'authenticated'; user: User }

export default function App() {
  const [state, setState] = useState<State>({ status: 'loading' })
  // O backend devolve a pessoa com ?erro=... quando o login com Google é recusado
  const [loginError] = useState(() => new URLSearchParams(window.location.search).get('erro'))

  useEffect(() => {
    if (loginError) window.history.replaceState(null, '', window.location.pathname)
  }, [loginError])

  useEffect(() => {
    let active = true
    getCurrentUser().then((user) => {
      if (active) setState(user ? { status: 'authenticated', user } : { status: 'anonymous' })
    })
    return () => {
      active = false
    }
  }, [])

  if (state.status === 'loading') return <main className="screen" aria-busy="true" />
  if (state.status === 'anonymous') return <LoginScreen error={loginError} />
  return (
    <HomeScreen
      user={state.user}
      onLogout={() => logout().then(() => setState({ status: 'anonymous' }))}
      onLogoutAll={() => logoutAll().then(() => setState({ status: 'anonymous' }))}
    />
  )
}
