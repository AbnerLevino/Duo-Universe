import { type ChangeEvent, type FormEvent, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Avatar from '@/shared/ui/Avatar'
import { errorMessage } from '@/shared/api/client'
import { chooseHandle } from '@/shared/api/account'
import type { MaritalStatus, MyProfile, ProfileVisibility } from '@/shared/api/types'
import { myProfile, removeAvatar, updateProfile, uploadAvatar } from './api'
import { prepareAvatar } from '@/shared/lib/imageTools'
import {
  BIO_MAX,
  MARITAL_LABELS,
  MARITAL_OPTIONS,
  PROFESSION_MAX,
  PROFILE_VISIBILITY_LABELS,
  TERMS,
} from '@/shared/lib/labels'
import { forgetMedia, type User } from '@/shared/session'

type Props = {
  user: User
  onUserChanged: (user: User) => void
  onLogout: () => void
  onLogoutAll: () => void
  /** Avisa o painel que o perfil mudou (nome, foto...) para ele recarregar. */
  onProfileChanged: () => void
  /** Fecha o modal (usado ao sair da tela por um link). */
  onClose: () => void
}

const HANDLE_FORMAT = /^[A-Za-z0-9_]{3,20}$/

/**
 * O conteúdo do modal "Editar perfil": foto, nome, bio, profissão, estado civil, visibilidade e o DuoName.
 * Não é mais uma tela: abre por cima da página atual, num modal que sobe de baixo (BottomSheet).
 */
export default function EditProfileForm({
  user,
  onUserChanged,
  onLogout,
  onLogoutAll,
  onProfileChanged,
  onClose,
}: Props) {
  const [profile, setProfile] = useState<MyProfile | null>(null)
  const [form, setForm] = useState({
    displayName: '',
    bio: '',
    profession: '',
    maritalStatus: 'SINGLE' as MaritalStatus,
    profileVisibility: 'PUBLIC' as ProfileVisibility,
  })
  const [handle, setHandle] = useState(user.handle ?? '')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    myProfile()
      .then((loaded) => {
        if (!active) return
        setProfile(loaded)
        setForm({
          displayName: loaded.displayName,
          bio: loaded.bio ?? '',
          profession: loaded.profession ?? '',
          maritalStatus: loaded.maritalStatus,
          profileVisibility: loaded.profileVisibility,
        })
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Não foi possível carregar o perfil.'))
      })
    return () => {
      active = false
    }
  }, [])

  async function save(event: FormEvent) {
    event.preventDefault()
    if (busy) return
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const saved = await updateProfile(form)
      setProfile(saved)
      onProfileChanged()
      onUserChanged({ ...user, displayName: saved.displayName })
      setMessage('Perfil salvo.')
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível salvar.'))
    } finally {
      setBusy(false)
    }
  }

  async function changeAvatar(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file || !profile) return
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const previous = profile.avatarMediaId
      const uploaded = await uploadAvatar(await prepareAvatar(file))
      if (previous) forgetMedia(previous)
      setProfile({ ...profile, avatarMediaId: uploaded.id })
      onProfileChanged()
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível enviar a foto.'))
    } finally {
      setBusy(false)
    }
  }

  async function dropAvatar() {
    if (!profile?.avatarMediaId) return
    if (!window.confirm('Remover a sua foto de perfil?')) return
    setBusy(true)
    setError(null)
    try {
      await removeAvatar()
      forgetMedia(profile.avatarMediaId)
      setProfile({ ...profile, avatarMediaId: null })
      onProfileChanged()
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível remover a foto.'))
    } finally {
      setBusy(false)
    }
  }

  async function changeHandle(event: FormEvent) {
    event.preventDefault()
    if (busy || handle === user.handle) return
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      onUserChanged(await chooseHandle(handle))
      setMessage(`${TERMS.duoName} atualizado.`)
    } catch (err) {
      setError(errorMessage(err, `Não foi possível trocar o ${TERMS.duoName}.`))
    } finally {
      setBusy(false)
    }
  }

  if (!profile)
    return (
      <p className="muted" aria-busy="true">
        Carregando…
      </p>
    )

  return (
    <div>
      <h2>Editar perfil</h2>

      <section className="profile-header">
        <Avatar name={profile.displayName} mediaId={profile.avatarMediaId} size="lg" />
        <div className="actions">
          <label className="button">
            Trocar foto
            <input type="file" accept="image/*" onChange={changeAvatar} hidden disabled={busy} />
          </label>
          {profile.avatarMediaId && (
            <button type="button" className="button subtle" onClick={dropAvatar} disabled={busy}>
              Remover foto
            </button>
          )}
        </div>
      </section>

      <form onSubmit={save} className="form">
        <label htmlFor="displayName">Nome</label>
        <input
          id="displayName"
          value={form.displayName}
          maxLength={50}
          onChange={(e) => setForm({ ...form, displayName: e.target.value })}
        />
        <label htmlFor="bio">
          Bio ({form.bio.length}/{BIO_MAX})
        </label>
        <textarea
          id="bio"
          value={form.bio}
          maxLength={BIO_MAX}
          rows={3}
          onChange={(e) => setForm({ ...form, bio: e.target.value })}
        />
        <label htmlFor="profession">Profissão</label>
        <input
          id="profession"
          value={form.profession}
          maxLength={PROFESSION_MAX}
          onChange={(e) => setForm({ ...form, profession: e.target.value })}
        />
        <label htmlFor="maritalStatus">Como você está?</label>
        <select
          id="maritalStatus"
          value={form.maritalStatus}
          onChange={(e) => setForm({ ...form, maritalStatus: e.target.value as MaritalStatus })}
        >
          {MARITAL_OPTIONS.map((option) => (
            <option key={option} value={option}>
              {MARITAL_LABELS[option]}
            </option>
          ))}
        </select>
        <label htmlFor="profileVisibility">Quem vê os seus momentos</label>
        <select
          id="profileVisibility"
          value={form.profileVisibility}
          onChange={(e) =>
            setForm({ ...form, profileVisibility: e.target.value as ProfileVisibility })
          }
        >
          {(Object.keys(PROFILE_VISIBILITY_LABELS) as ProfileVisibility[]).map((option) => (
            <option key={option} value={option}>
              {PROFILE_VISIBILITY_LABELS[option]}
            </option>
          ))}
        </select>
        <p className="muted">
          Público: qualquer pessoa vê o que você marcar como público. Privado: só quem você aceitar
          como {TERMS.followers}. O seu cartão (nome, foto e bio) aparece nos dois casos.
        </p>
        <button type="submit" className="button primary" disabled={busy}>
          Salvar
        </button>
      </form>

      <form onSubmit={changeHandle} className="form">
        <label htmlFor="duoName">{TERMS.duoName}</label>
        <input id="duoName" value={handle} onChange={(e) => setHandle(e.target.value)} />
        <p className="muted">
          3 a 20 letras, números ou &quot;_&quot;. Você pode trocar 2 vezes por dia.
        </p>
        <button
          type="submit"
          className="button"
          disabled={busy || !HANDLE_FORMAT.test(handle) || handle === user.handle}
        >
          Trocar {TERMS.duoName}
        </button>
      </form>

      {message && <p role="status">{message}</p>}
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}

      <section className="actions">
        <Link to="/lista/bloqueados" className="button subtle" onClick={onClose}>
          Pessoas bloqueadas
        </Link>
        <button type="button" className="button" onClick={onLogout}>
          Sair
        </button>
        <button type="button" className="button subtle" onClick={onLogoutAll}>
          Sair de todos os aparelhos
        </button>
      </section>
    </div>
  )
}
