import { fireEvent, screen } from '@testing-library/react'
import PublicProfileScreen from './PublicProfileScreen'
import { clearSession } from '@/shared/session'
import { callsTo, jsonResponse, mockBackend, renderAt } from '@/shared/test/testHelpers'

const MARIA = {
  userId: 7,
  displayName: 'Maria Souza',
  handle: 'maria',
  bio: 'Oi!',
  profession: 'Designer',
  maritalStatus: 'DATING',
  profileVisibility: 'PUBLIC',
  avatarMediaId: null,
  duollowers: 12,
  duollowing: 5,
  relation: 'NONE',
  self: false,
  canSeeContent: true,
}
const PHOTO = {
  id: 10,
  ownerUserId: 7,
  kind: 'PHOTO',
  visibility: 'PUBLIC',
  caption: 'Praia',
  contentType: 'image/png',
  sizeBytes: 100,
  createdAt: '2026-11-06T10:00:00Z',
}

const open = () =>
  renderAt('/u/:handle', '/u/maria', <PublicProfileScreen />, {
    '/inicio': <p>Página inicial</p>,
    '/buscar': <p>Tela de busca</p>,
  })

describe('PublicProfileScreen', () => {
  beforeEach(() => clearSession())
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('mostra o cartão, os números e as fotos que a pessoa pode ver', async () => {
    mockBackend({
      'GET /api/profiles/maria': () => jsonResponse(200, MARIA),
      'GET /api/media/users/7/photos': () => jsonResponse(200, [PHOTO]),
      'GET /api/media/10': () => new Response(new Blob(['x'], { type: 'image/png' })),
    })
    open()

    expect(await screen.findByRole('heading', { name: 'Maria Souza' })).toBeInTheDocument()
    expect(screen.getByText('@maria')).toBeInTheDocument()
    expect(screen.getByText('Designer · Namorando')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Abrir foto: Praia' })).toBeInTheDocument()
  })

  it('perfil privado sem acesso mostra o aviso e nem pede as fotos', async () => {
    const fetchMock = mockBackend({
      'GET /api/profiles/maria': () =>
        jsonResponse(200, { ...MARIA, profileVisibility: 'PRIVATE', canSeeContent: false }),
    })
    open()

    expect(await screen.findByText(/Este perfil é privado/)).toBeInTheDocument()
    expect(callsTo(fetchMock, 'GET /api/media/users/7/photos')).toHaveLength(0)
  })

  it('pedido pendente explica que os momentos aparecem quando for aceito', async () => {
    mockBackend({
      'GET /api/profiles/maria': () =>
        jsonResponse(200, {
          ...MARIA,
          profileVisibility: 'PRIVATE',
          canSeeContent: false,
          relation: 'REQUESTED',
        }),
    })
    open()

    expect(await screen.findByText(/Pedido enviado\. Os momentos aparecem/)).toBeInTheDocument()
  })

  it('o próprio perfil redireciona para a página inicial', async () => {
    mockBackend({ 'GET /api/profiles/maria': () => jsonResponse(200, { ...MARIA, self: true }) })
    open()

    expect(await screen.findByText('Página inicial')).toBeInTheDocument()
  })

  it('perfil inexistente, oculto ou bloqueado mostra a mesma mensagem', async () => {
    mockBackend({
      'GET /api/profiles/maria': () => jsonResponse(404, { detail: 'Perfil não encontrado' }),
    })
    open()

    expect(await screen.findByRole('alert')).toHaveTextContent('Perfil não encontrado')
  })

  it('bloquear pede confirmação, bloqueia e volta para a busca', async () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true)
    const fetchMock = mockBackend({
      'GET /api/profiles/maria': () => jsonResponse(200, { ...MARIA, canSeeContent: false }),
      'PUT /api/me/blocks/maria': () => jsonResponse(204),
    })
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Bloquear' }))

    expect(await screen.findByText('Tela de busca')).toBeInTheDocument()
    expect(confirm).toHaveBeenCalled()
    expect(callsTo(fetchMock, 'PUT /api/me/blocks/maria')).toHaveLength(1)
  })

  it('se a pessoa desistir na confirmação, ninguém é bloqueado', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    const fetchMock = mockBackend({
      'GET /api/profiles/maria': () => jsonResponse(200, { ...MARIA, canSeeContent: false }),
    })
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Bloquear' }))

    expect(callsTo(fetchMock, 'PUT /api/me/blocks/maria')).toHaveLength(0)
  })

  it('denunciar o perfil envia o motivo escolhido', async () => {
    const fetchMock = mockBackend({
      'GET /api/profiles/maria': () => jsonResponse(200, { ...MARIA, canSeeContent: false }),
      'POST /api/profiles/maria/report': () => jsonResponse(204),
    })
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Denunciar perfil' }))
    fireEvent.change(screen.getByLabelText('Motivo'), { target: { value: 'FAKE_PROFILE' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enviar denúncia' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Denúncia enviada')
    const [, init] = callsTo(fetchMock, 'POST /api/profiles/maria/report')[0]
    expect(JSON.parse(String(init?.body))).toEqual({ reason: 'FAKE_PROFILE', details: '' })
  })
})
