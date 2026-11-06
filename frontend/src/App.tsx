import { useEffect, useState } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import AcceptInviteScreen from './AcceptInviteScreen'
import ConfirmScreen from './ConfirmScreen'
import CoupleHomeScreen from './CoupleHomeScreen'
import Dashboard from './Dashboard'
import DateConsensusScreen from './DateConsensusScreen'
import EditProfileScreen from './EditProfileScreen'
import FollowListScreen from './FollowListScreen'
import HandleSetupScreen from './HandleSetupScreen'
import HomeScreen from './HomeScreen'
import InviteScreen from './InviteScreen'
import LoginScreen from './LoginScreen'
import NewMomentScreen from './NewMomentScreen'
import PublicProfileScreen from './PublicProfileScreen'
import SearchScreen from './SearchScreen'
import { getCurrentUser, logout, logoutAll, type User } from './session'
import StoryScreen from './StoryScreen'

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

  const { user } = state
  const onLogout = () => logout().then(() => setState({ status: 'anonymous' }))
  const onLogoutAll = () => logoutAll().then(() => setState({ status: 'anonymous' }))
  const onUserChanged = (updated: User) => setState({ status: 'authenticated', user: updated })

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/inicio" element={<HomeScreen user={user} />} />
        <Route path="/buscar" element={<SearchScreen />} />
        <Route path="/u/:handle" element={<PublicProfileScreen />} />
        <Route path="/lista/:kind" element={<FollowListScreen />} />
        <Route path="/momentos/novo" element={<NewMomentScreen />} />
        <Route
          path="/perfil/editar"
          element={
            <EditProfileScreen
              user={user}
              onUserChanged={onUserChanged}
              onLogout={onLogout}
              onLogoutAll={onLogoutAll}
            />
          }
        />
        <Route path="/convite/:code" element={<AcceptInviteScreen user={user} />} />
        <Route path="/vinculo/handle" element={<HandleSetupScreen onChosen={onUserChanged} />} />
        <Route path="/vinculo/convidar" element={<InviteScreen />} />
        <Route path="/vinculo/:pairingId/historia" element={<StoryScreen />} />
        <Route path="/vinculo/:pairingId/datas" element={<DateConsensusScreen user={user} />} />
        <Route path="/vinculo/:pairingId" element={<ConfirmScreen user={user} />} />
        <Route
          path="/casal"
          element={<CoupleHomeScreen user={user} onLogout={onLogout} onLogoutAll={onLogoutAll} />}
        />
        <Route path="*" element={<Dashboard user={user} />} />
      </Routes>
    </BrowserRouter>
  )
}
