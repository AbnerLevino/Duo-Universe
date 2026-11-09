import { useCallback, useEffect, useMemo, useState } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { followRequests, getProfile, myProfile, type MyProfile } from './api'
import BottomSheet from './BottomSheet'
import EditProfileForm from './EditProfileForm'
import MainNav from './MainNav'
import { socialEnabled } from './features'
import { type Me, MeContext } from './meContext'
import SideProfile from './SideProfile'
import type { User } from './session'

type Props = {
  user: User
  onUserChanged: (user: User) => void
  onLogout: () => void
  onLogoutAll: () => void
}

type Loaded = {
  handle: string
  profile: MyProfile
  duollowers: number
  duollowing: number
  requests: number
}

/**
 * A moldura de todas as telas de quem está logado: o painel à esquerda (a sua foto, os números e a navegação) e,
 * ao lado, a tela da rota atual (`Outlet`). No celular o painel se divide: o resumo vira a faixa do topo e a
 * navegação, a faixa de baixo. O layout carrega o seu perfil uma vez e reparte com as telas (`useMe`).
 */
export default function AppLayout({ user, onUserChanged, onLogout, onLogoutAll }: Props) {
  const [version, setVersion] = useState(0)
  const [editOpen, setEditOpen] = useState(false)
  const [loaded, setLoaded] = useState<Loaded | null>(null)

  useEffect(() => {
    if (!user.handle) return
    const handle = user.handle
    let active = true
    // Congelado (RF-A11): sem a camada social, só o próprio perfil existe para buscar.
    const social = socialEnabled()
    Promise.all([
      myProfile(),
      social ? getProfile(handle) : Promise.resolve({ duollowers: 0, duollowing: 0 }),
      social ? followRequests() : Promise.resolve([]),
    ])
      .then(([profile, counts, requests]) => {
        if (active) {
          setLoaded({
            handle,
            profile,
            duollowers: counts.duollowers,
            duollowing: counts.duollowing,
            requests: requests.length,
          })
        }
      })
      .catch(() => {
        // sem os dados o painel só mostra o espaço da foto e zeros; as telas seguem funcionando
      })
    return () => {
      active = false
    }
  }, [user.handle, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])
  const openEdit = useCallback(() => setEditOpen(true), [])
  const closeEdit = useCallback(() => setEditOpen(false), [])

  const current = loaded?.handle === user.handle ? loaded : null
  const me = useMemo<Me>(
    () => ({
      profile: current?.profile ?? null,
      duollowers: current?.duollowers ?? 0,
      duollowing: current?.duollowing ?? 0,
      requests: current?.requests ?? 0,
      reload,
      openEdit,
    }),
    [current, reload, openEdit],
  )

  if (!user.handle) return <Navigate to="/vinculo/handle" replace />

  return (
    <MeContext.Provider value={me}>
      <div className="app-shell">
        <aside className="side">
          <SideProfile />
          <MainNav onProfile={openEdit} />
        </aside>
        <div className="app-content">
          <Outlet />
        </div>
      </div>
      {editOpen && (
        <BottomSheet title="Editar perfil" onClose={closeEdit}>
          <EditProfileForm
            user={user}
            onUserChanged={onUserChanged}
            onLogout={onLogout}
            onLogoutAll={onLogoutAll}
            onProfileChanged={reload}
            onClose={closeEdit}
          />
        </BottomSheet>
      )}
    </MeContext.Provider>
  )
}
