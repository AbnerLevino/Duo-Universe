import { fireEvent, render, screen } from '@testing-library/react'
import BottomSheet from './BottomSheet'

function open(onClose = vi.fn()) {
  render(
    <BottomSheet title="Editar perfil" onClose={onClose}>
      <button type="button">Conteúdo</button>
    </BottomSheet>,
  )
  return {
    onClose,
    sheet: screen.getByRole('dialog', { name: 'Editar perfil' }),
    handle: screen.getByRole('button', { name: 'Expandir' }),
  }
}

/** Puxa a alça: o dedo desce em `fromY` e solta em `toY` (menor = mais para cima). */
function drag(handle: HTMLElement, fromY: number, toY: number) {
  fireEvent.pointerDown(handle, { clientY: fromY, pointerId: 1 })
  fireEvent.pointerMove(handle, { clientY: toY, pointerId: 1 })
  fireEvent.pointerUp(handle, { clientY: toY, pointerId: 1 })
}

describe('BottomSheet', () => {
  it('abre na metade da altura da tela, como um diálogo', () => {
    const { sheet } = open()

    expect(sheet).toHaveAttribute('data-mode', 'half')
    expect(sheet).toHaveAttribute('aria-modal', 'true')
    expect(sheet).toHaveStyle({ height: `${window.innerHeight * 0.5}px` })
    expect(screen.getByRole('button', { name: 'Conteúdo' })).toBeInTheDocument()
  })

  it('puxar a alça para cima abre ele inteiro, e a alça passa a oferecer "Diminuir"', () => {
    const { sheet, handle } = open()

    drag(handle, 400, 200)

    expect(sheet).toHaveAttribute('data-mode', 'full')
    expect(sheet).toHaveStyle({ height: `${window.innerHeight - 8}px` })
    expect(screen.getByRole('button', { name: 'Diminuir' })).toHaveAttribute(
      'aria-expanded',
      'true',
    )
  })

  it('puxar para baixo estando inteiro volta para a metade, sem fechar', () => {
    const { onClose, sheet, handle } = open()
    drag(handle, 400, 200)

    drag(screen.getByRole('button', { name: 'Diminuir' }), 200, 400)

    expect(sheet).toHaveAttribute('data-mode', 'half')
    expect(onClose).not.toHaveBeenCalled()
  })

  it('puxar para baixo estando na metade fecha', () => {
    const { onClose, handle } = open()

    drag(handle, 300, 500)

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('uma puxada curta demais não faz nada', () => {
    const { onClose, sheet, handle } = open()

    drag(handle, 300, 270)

    expect(sheet).toHaveAttribute('data-mode', 'half')
    expect(onClose).not.toHaveBeenCalled()
  })

  it('tocar na alça (ou Enter) alterna entre a metade e o inteiro', () => {
    const { sheet, handle } = open()

    fireEvent.click(handle)
    expect(sheet).toHaveAttribute('data-mode', 'full')
    fireEvent.click(screen.getByRole('button', { name: 'Diminuir' }))
    expect(sheet).toHaveAttribute('data-mode', 'half')
  })

  it('o Esc e o clique no fundo fecham; o clique dentro não', () => {
    const { onClose, sheet } = open()

    fireEvent.click(sheet)
    expect(onClose).not.toHaveBeenCalled()
    fireEvent.keyDown(document, { key: 'Escape' })
    expect(onClose).toHaveBeenCalledTimes(1)
    fireEvent.click(sheet.parentElement as HTMLElement)
    expect(onClose).toHaveBeenCalledTimes(2)
  })

  it('trava a rolagem da página de trás enquanto está aberto e solta ao sair', () => {
    const { unmount } = render(
      <BottomSheet title="x" onClose={vi.fn()}>
        <p>a</p>
      </BottomSheet>,
    )
    expect(document.body.style.overflow).toBe('hidden')

    unmount()

    expect(document.body.style.overflow).not.toBe('hidden')
  })
})
