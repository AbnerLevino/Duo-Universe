import { Link } from 'react-router-dom'
import Avatar from '@/shared/ui/Avatar'
import { socialEnabled } from '@/shared/lib/features'
import { TERMS } from '@/shared/lib/labels'
import { useMe } from '@/shared/context/meContext'

/**
 * O resumo do seu perfil: a foto grande e, na mesma linha, Duollowers, Duollowing e Editar. No computador fica no
 * topo do painel à esquerda; no celular vira a faixa do topo da tela (quem decide é o CSS).
 */
export default function SideProfile() {
  const { profile, duollowers, duollowing, openEdit } = useMe()

  return (
    <section className="side-profile" aria-label="Seu perfil">
      <Avatar
        name={profile?.displayName ?? ''}
        mediaId={profile?.avatarMediaId ?? null}
        size="xl"
      />
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
