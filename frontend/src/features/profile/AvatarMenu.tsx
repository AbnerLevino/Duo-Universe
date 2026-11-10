import { type ChangeEvent, useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import Avatar from '@/shared/ui/Avatar'
import { errorMessage } from '@/shared/api/client'
import { useMe } from '@/shared/context/meContext'
import { prepareAvatar } from '@/shared/lib/imageTools'
import { useMediaQuery } from '@/shared/lib/useMediaQuery'
import { forgetMedia } from '@/shared/session'
import { uploadAvatar } from './api'
import styles from './AvatarMenu.module.css'

/** O mesmo ponto em que o painel lateral some e vira as faixas do celular (layout.css). */
const DESKTOP = '(min-width: 768px)'

/**
 * A foto do painel lateral. No computador ela é um botão: ao clicar, abre um cartãozinho ao lado com a ação da
 * foto ("Colocar foto de perfil" se ainda não há, "Trocar foto de perfil" se já há). No celular ela continua só
 * uma imagem: como o perfil aparece por lá ainda vai ser pensado.
 */
export default function AvatarMenu() {
  const { profile, reload } = useMe()
  const desktop = useMediaQuery(DESKTOP)
  const [open, setOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [position, setPosition] = useState({ left: 0, top: 0 })
  const triggerRef = useRef<HTMLButtonElement>(null)
  const menuRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const hasPhoto = profile?.avatarMediaId != null

  // Aberto: Esc ou clique fora fecham; mudar o tamanho da janela também (a posição é calculada ao abrir).
  useEffect(() => {
    if (!open) return
    const close = () => setOpen(false)
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        close()
        triggerRef.current?.focus()
      }
    }
    const onPointerDown = (event: PointerEvent) => {
      const target = event.target as Node
      if (menuRef.current?.contains(target) || triggerRef.current?.contains(target)) return
      close()
    }
    document.addEventListener('keydown', onKey)
    document.addEventListener('pointerdown', onPointerDown)
    window.addEventListener('resize', close)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.removeEventListener('pointerdown', onPointerDown)
      window.removeEventListener('resize', close)
    }
  }, [open])

  function toggle() {
    if (!open) {
      const rect = triggerRef.current?.getBoundingClientRect()
      // `fixed` (e não `absolute`) porque o painel lateral rola por dentro e cortaria o cartão que sai dele.
      if (rect) setPosition({ left: rect.right + 12, top: rect.top })
      setError(null)
    }
    setOpen(!open)
  }

  async function onFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setBusy(true)
    setError(null)
    try {
      const previous = profile?.avatarMediaId ?? null
      await uploadAvatar(await prepareAvatar(file))
      if (previous) forgetMedia(previous)
      reload()
      setOpen(false)
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível enviar a foto.'))
    } finally {
      setBusy(false)
    }
  }

  const avatar = (
    <Avatar name={profile?.displayName ?? ''} mediaId={profile?.avatarMediaId ?? null} size="xl" />
  )
  if (!desktop) return avatar

  return (
    <div className={styles.wrap}>
      <button
        type="button"
        ref={triggerRef}
        className={styles.trigger}
        aria-haspopup="dialog"
        aria-expanded={open}
        aria-label="Opções da foto de perfil"
        onClick={toggle}
      >
        {avatar}
      </button>
      {/* Fica sempre montado: o seletor de arquivos do sistema abre por cima, e o `change` precisa de onde cair. */}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        hidden
        disabled={busy}
        onChange={onFile}
        data-testid="avatar-file"
      />
      {open &&
        createPortal(
          <div
            ref={menuRef}
            role="dialog"
            aria-label="Foto de perfil"
            className={styles.menu}
            style={position}
          >
            <button
              type="button"
              className={styles.option}
              disabled={busy}
              onClick={() => inputRef.current?.click()}
            >
              {busy ? 'Enviando…' : hasPhoto ? 'Trocar foto de perfil' : 'Colocar foto de perfil'}
            </button>
            {error && (
              <p role="alert" className={styles.error}>
                {error}
              </p>
            )}
          </div>,
          document.body,
        )}
    </div>
  )
}
