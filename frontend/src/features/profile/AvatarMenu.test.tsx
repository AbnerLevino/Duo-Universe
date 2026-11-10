import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import AvatarMenu from './AvatarMenu'
import { type Me, MeContext } from '@/shared/context/meContext'
import type { MyProfile } from '@/shared/api/types'
import { clearSession } from '@/shared/session'
import { callsTo, jsonResponse, mockBackend } from '@/shared/test/testHelpers'

vi.mock('@/shared/lib/imageTools', () => ({
  prepareAvatar: vi.fn(async (file: File) => file),
}))

const PROFILE: MyProfile = {
  id: 1,
  displayName: 'Ana',
  handle: 'ana',
  bio: null,
  profession: null,
  maritalStatus: 'SINGLE',
  profileVisibility: 'PUBLIC',
  avatarMediaId: null,
  hidden: false,
}

function open(profile: MyProfile = PROFILE, reload = vi.fn()) {
  const me: Me = {
    profile,
    duollowers: 0,
    duollowing: 0,
    requests: 0,
    reload,
    openEdit: vi.fn(),
  }
  render(
    <MeContext.Provider value={me}>
      <AvatarMenu />
    </MeContext.Provider>,
  )
  return reload
}

describe('AvatarMenu (computador)', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('sem foto, oferece "Colocar foto de perfil"', () => {
    open()

    fireEvent.click(screen.getByRole('button', { name: 'Opções da foto de perfil' }))

    expect(screen.getByRole('button', { name: 'Colocar foto de perfil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Trocar foto de perfil' })).not.toBeInTheDocument()
  })

  it('com foto, oferece "Trocar foto de perfil"', () => {
    mockBackend({ 'GET /api/media/4': () => jsonResponse(404) })
    open({ ...PROFILE, avatarMediaId: 4 })

    fireEvent.click(screen.getByRole('button', { name: 'Opções da foto de perfil' }))

    expect(screen.getByRole('button', { name: 'Trocar foto de perfil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Colocar foto de perfil' })).not.toBeInTheDocument()
  })

  it('Esc e clique fora fecham o cartão', () => {
    open()
    const trigger = screen.getByRole('button', { name: 'Opções da foto de perfil' })

    fireEvent.click(trigger)
    expect(screen.getByRole('dialog', { name: 'Foto de perfil' })).toBeInTheDocument()
    fireEvent.keyDown(document, { key: 'Escape' })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    fireEvent.click(trigger)
    fireEvent.pointerDown(document.body)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('escolher o arquivo envia a foto, recarrega o painel e fecha o cartão', async () => {
    const fetchMock = mockBackend({
      'POST /api/me/avatar': () => jsonResponse(200, { id: 9 }),
    })
    const reload = open()
    fireEvent.click(screen.getByRole('button', { name: 'Opções da foto de perfil' }))

    const file = new File(['x'], 'eu.png', { type: 'image/png' })
    fireEvent.change(screen.getByTestId('avatar-file'), { target: { files: [file] } })

    await waitFor(() => expect(reload).toHaveBeenCalledTimes(1))
    expect(callsTo(fetchMock, 'POST /api/me/avatar')).toHaveLength(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('se o envio falha, o cartão continua aberto com o aviso', async () => {
    mockBackend({ 'POST /api/me/avatar': () => jsonResponse(500) })
    const reload = open()
    fireEvent.click(screen.getByRole('button', { name: 'Opções da foto de perfil' }))

    const file = new File(['x'], 'eu.png', { type: 'image/png' })
    fireEvent.change(screen.getByTestId('avatar-file'), { target: { files: [file] } })

    expect(await screen.findByRole('alert')).toHaveTextContent(/Não foi possível/)
    expect(reload).not.toHaveBeenCalled()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })
})

describe('AvatarMenu (celular)', () => {
  beforeEach(() => {
    clearSession()
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: false,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
  })
  afterEach(() => vi.unstubAllGlobals())

  it('a foto não vira botão: o perfil no celular ainda vai ser pensado', () => {
    open()

    expect(
      screen.queryByRole('button', { name: 'Opções da foto de perfil' }),
    ).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Foto de Ana' })).toBeInTheDocument()
  })
})
