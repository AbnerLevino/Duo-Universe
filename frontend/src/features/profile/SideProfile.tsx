import { Link } from 'react-router-dom'
import AvatarMenu from './AvatarMenu'
import { socialEnabled } from '@/shared/lib/features'
import { MARITAL_LABELS, TERMS } from '@/shared/lib/labels'
import { useMe } from '@/shared/context/meContext'

/**
 * O resumo do seu perfil: a foto, o nome, o DuoName e a situação (profissão · estado civil) e, na mesma linha,
 * Duollowers, Duollowing e Editar. No computador fica no topo do painel à esquerda; no celular vira a faixa do
 * topo da tela, sem o nome (quem decide é o CSS; lá o nome ainda aparece na `HomeScreen`).
 */
export default function SideProfile() {
  const { profile, duollowers, duollowing, openEdit } = useMe()
  const situation = profile
    ? [profile.profession, MARITAL_LABELS[profile.maritalStatus]].filter(Boolean).join(' · ')
    : ''

  return (
    <section className="side-profile" aria-label="Seu perfil">
      <AvatarMenu />
      {profile && (
        <div className="side-info">
          <strong className="side-name">{profile.displayName}</strong>
          <span>@{profile.handle}</span>
          {situation && <span>{situation}</span>}
        </div>
      )}
      <ul className="side-stats">
        {socialEnabled() && (
          <>
            <li>
              <Link to="/lista/duollowers">
                <strong>{duollowers}</strong>
                <span>{TERMS.followers}</span>
              </Link>
            </li>
            <li>
              <Link to="/lista/duollowing">
                <strong>{duollowing}</strong>
                <span>{TERMS.following}</span>
              </Link>
            </li>
          </>
        )}
        <li>
          <button type="button" className="side-edit" onClick={openEdit}>
            Editar
          </button>
        </li>
      </ul>
    </section>
  )
}
