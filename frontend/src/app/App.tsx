import { useEffect, useState } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import AcceptInviteScreen from '@/features/relationships/AcceptInviteScreen'
import AppLayout from './AppLayout'
import ConfirmScreen from '@/features/relationships/ConfirmScreen'
import CoupleHomeScreen from '@/features/relationships/CoupleHomeScreen'
import Dashboard from './Dashboard'
import DateConsensusScreen from '@/features/relationships/DateConsensusScreen'
import FollowListScreen from '@/features/social/FollowListScreen'
import HandleSetupScreen from '@/features/auth/HandleSetupScreen'
import HomeScreen from '@/features/profile/HomeScreen'
import InviteScreen from '@/features/relationships/InviteScreen'
import LoginScreen from '@/features/auth/LoginScreen'
import NewMomentScreen from '@/features/media/NewMomentScreen'
import PublicProfileScreen from '@/features/profile/PublicProfileScreen'
import SearchScreen from '@/features/social/SearchScreen'
import { socialEnabled } from '@/shared/lib/features'
import { getCurrentUser, logout, logoutAll, type User } from '@/shared/session'
import StoryScreen from '@/features/relationships/StoryScreen'

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
        {/* Antes de ter um DuoName ainda não existe perfil: esta tela fica fora da moldura. */}
        <Route path="/vinculo/handle" element={<HandleSetupScreen onChosen={onUserChanged} />} />
        {/* Todas as outras telas de quem está logado dividem o painel à esquerda (AppLayout). */}
        <Route
          element={
            <AppLayout
              user={user}
              onUserChanged={onUserChanged}
              onLogout={onLogout}
              onLogoutAll={onLogoutAll}
            />
          }
        >
          <Route path="/inicio" element={<HomeScreen />} />
          {/* Camada social congelada (RF-A11): sem busca nem perfil de outras pessoas. */}
          {socialEnabled() && <Route path="/buscar" element={<SearchScreen />} />}
          {socialEnabled() && <Route path="/u/:handle" element={<PublicProfileScreen />} />}
          <Route path="/lista/:kind" element={<FollowListScreen />} />
          <Route path="/momentos/novo" element={<NewMomentScreen />} />
          <Route path="/convite/:code" element={<AcceptInviteScreen user={user} />} />
          <Route path="/vinculo/convidar" element={<InviteScreen />} />
          <Route path="/vinculo/:pairingId/historia" element={<StoryScreen />} />
          <Route path="/vinculo/:pairingId/datas" element={<DateConsensusScreen user={user} />} />
          <Route path="/vinculo/:pairingId" element={<ConfirmScreen user={user} />} />
          <Route
            path="/casal"
            element={<CoupleHomeScreen user={user} onLogout={onLogout} onLogoutAll={onLogoutAll} />}
          />
          <Route path="*" element={<Dashboard user={user} />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
