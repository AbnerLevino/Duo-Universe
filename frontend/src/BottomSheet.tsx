import { type PointerEvent, type ReactNode, useEffect, useRef, useState } from 'react'

type Props = {
  title: string
  onClose: () => void
  children: ReactNode
}

type Mode = 'half' | 'full'

/** Quanto o dedo (ou o mouse) precisa andar, em pixels, para a puxada valer. */
const DRAG_THRESHOLD = 60
/** Abaixo disso é um toque, não uma puxada. */
const TAP_LIMIT = 5
const FULL_MARGIN = 8

/**
 * Um modal que sobe de baixo, com o topo arredondado (a "barriga"). Abre na metade da altura da tela; puxar a
 * alça para cima abre ele inteiro, puxar para baixo volta para a metade e, estando na metade, fecha. A alça também
 * é um botão (Enter ou Espaço alternam), o Esc e o clique no fundo fecham. A página de trás não rola enquanto
 * ele estiver aberto.
 */
export default function BottomSheet({ title, onClose, children }: Props) {
  const [mode, setMode] = useState<Mode>('half')
  const [drag, setDrag] = useState<number | null>(null)
  const sheetRef = useRef<HTMLDivElement>(null)
  const startY = useRef(0)
  const dragged = useRef(false)
  const closeRef = useRef(onClose)

  useEffect(() => {
    closeRef.current = onClose
  })

  useEffect(() => {
    const previousFocus = document.activeElement as HTMLElement | null
    const previousOverflow = document.body.style.overflow
    sheetRef.current?.focus()
    document.body.style.overflow = 'hidden'
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') closeRef.current()
    }
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = previousOverflow
      previousFocus?.focus?.()
    }
  }, [])

  const viewport = window.innerHeight
  const half = viewport * 0.5
  const full = viewport - FULL_MARGIN
  const base = mode === 'full' ? full : half
  // enquanto arrasta, a altura acompanha o dedo, sem passar do inteiro nem encolher demais
  const height = drag === null ? base : Math.min(full, Math.max(half * 0.4, base + drag))

  function onPointerDown(event: PointerEvent<HTMLButtonElement>) {
    startY.current = event.clientY
    dragged.current = false
    event.currentTarget.setPointerCapture?.(event.pointerId)
    setDrag(0)
  }

  function onPointerMove(event: PointerEvent<HTMLButtonElement>) {
    if (drag === null) return
    const up = startY.current - event.clientY
    if (Math.abs(up) > TAP_LIMIT) dragged.current = true
    setDrag(up)
  }

  function onPointerUp() {
    if (drag === null) return
    if (drag > DRAG_THRESHOLD) setMode('full')
    else if (drag < -DRAG_THRESHOLD) {
      if (mode === 'full') setMode('half')
      else onClose()
    }
    setDrag(null)
  }

  function toggle() {
    // depois de uma puxada o navegador ainda dispara um clique na alça; esse não conta como toque
    if (dragged.current) {
      dragged.current = false
      return
    }
    setMode(mode === 'full' ? 'half' : 'full')
  }

  return (
    <div className="sheet-backdrop" onClick={onClose}>
      <div
        ref={sheetRef}
        className="sheet"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        data-mode={mode}
        data-dragging={drag !== null}
        tabIndex={-1}
        style={{ height }}
        onClick={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          className="sheet-handle"
          aria-label={mode === 'full' ? 'Diminuir' : 'Expandir'}
          aria-expanded={mode === 'full'}
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
          onPointerCancel={() => setDrag(null)}
          onClick={toggle}
        >
          <span className="sheet-grip" />
        </button>
        <div className="sheet-body">{children}</div>
      </div>
    </div>
  )
}
