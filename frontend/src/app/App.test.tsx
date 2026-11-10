import { fireEvent, render, screen } from '@testing-library/react'
import { StrictMode } from 'react'
import App from './App'
import { clearSession } from '@/shared/session'

const jsonResponse = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status })

const ME = { id: 1, email: 'ana@example.com', displayName: 'Ana', handle: 'ana' }
const TOKEN = { accessToken: 'jwt', expiresInSeconds: 900 }
const MY_PROFILE = {
  id: 1,
  displayName: 'Ana',
  handle: 'ana',
  bio: 'Gosto de café',
  profession: 'Dev',
  maritalStatus: 'SINGLE',
  profileVisibility: 'PUBLIC',
  avatarMediaId: null,
  hidden: false,
}
const MY_PUBLIC_PROFILE = {
  userId: 1,
  displayName: 'Ana',
  handle: 'ana',
  bio: 'Gosto de café',
  profession: 'Dev',
  maritalStatus: 'SINGLE',
  profileVisibility: 'PUBLIC',
  avatarMediaId: null,
  duollowers: 3,
  duollowing: 2,
  relation: 'NONE',
  self: true,
  canSeeContent: true,
}
const COUPLE = {
  id: 1,
  state: 'ACTIVE',
  knownSinceDate: null,
  datingSinceDate: null,
  pausedAt: null,
  endedAt: null,
}

/** Simula o backend: o que cada rota responde. Sessão com casal ativo (o caminho mais comum nos testes
 * abaixo) chega direto na CoupleHomeScreen, que também mostra o nome da pessoa. */
function mockBackend(routes: Record<string, () => Response>) {
  const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
    const key = `${init?.method ?? 'GET'} ${String(input)}`
    const handler = routes[key]
    return Promise.resolve(handler ? handler() : jsonResponse(404))
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

const withCouple = {
  'GET /api/couples/mine': () => jsonResponse(200, COUPLE),
  'GET /api/pairings/mine': () => jsonResponse(200, []),
}

describe('App', () => {
  beforeEach(() => {
    clearSession()
    window.history.replaceState(null, '', '/')
  })
  afterEach(() => vi.unstubAllGlobals())

  it('sem sessão mostra o botão de entrar com Google', async () => {
    mockBackend({ 'POST /api/auth/refresh': () => jsonResponse(401) })
    render(<App />)

    const link = await screen.findByRole('link', { name: 'Entrar com Google' })
    expect(link).toHaveAttribute('href', expect.stringContaining('/oauth2/authorization/google'))
  })

  it('com sessão e casal ativo mostra o nome da pessoa', async () => {
    mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
      ...withCouple,
    })
    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Ana' })).toBeInTheDocument()
  })

  it('sem @handle escolhido, manda para a tela de escolher um antes de qualquer outra coisa', async () => {
    mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, { ...ME, handle: null }),
    })
    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Escolha seu DuoName' })).toBeInTheDocument()
  })

  it('solteiro cai no Personal Universe, com o vínculo como um caminho dentro da home', async () => {
    mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
      'GET /api/couples/mine': () => jsonResponse(204),
      'GET /api/pairings/mine': () => jsonResponse(200, []),
      'GET /api/me/profile': () => jsonResponse(200, MY_PROFILE),
      'GET /api/profiles/ana': () => jsonResponse(200, MY_PUBLIC_PROFILE),
      'GET /api/media/users/1/photos': () => jsonResponse(200, []),
      'GET /api/me/follow-requests': () => jsonResponse(200, []),
    })
    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Ana' })).toBeInTheDocument()
    expect(window.location.pathname).toBe('/inicio')
    expect(screen.getByRole('link', { name: 'Vincular-se' })).toHaveAttribute(
      'href',
      '/vinculo/convidar',
    )
    expect(screen.getByRole('link', { name: /Duollowers/ })).toBeInTheDocument()
  })

  it('mostra o motivo quando o backend recusa o login', async () => {
    window.history.replaceState(null, '', '/?erro=email-ja-vinculado')
    mockBackend({ 'POST /api/auth/refresh': () => jsonResponse(401) })
    render(<App />)

    expect(await screen.findByRole('alert')).toHaveTextContent('já está ligado a outra conta')
    expect(window.location.search).toBe('')
  })

  it('sair encerra a sessão e volta para a tela de entrada', async () => {
    const fetchMock = mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
      ...withCouple,
      'POST /api/auth/logout': () => jsonResponse(204),
    })
    render(<App />)

    fireEvent.click(await screen.findByRole('button', { name: 'Sair' }))

    expect(await screen.findByRole('link', { name: 'Entrar com Google' })).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/logout', { method: 'POST' })
  })

  it('sair de todos os aparelhos usa o token de acesso', async () => {
    const fetchMock = mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
      ...withCouple,
      'POST /api/auth/logout-all': () => jsonResponse(204),
    })
    render(<App />)

    fireEvent.click(await screen.findByRole('button', { name: 'Sair de todos os aparelhos' }))

    expect(await screen.findByRole('link', { name: 'Entrar com Google' })).toBeInTheDocument()
    const call = fetchMock.mock.calls.find(([url]) => url === '/api/auth/logout-all')
    expect(call?.[1]?.headers).toMatchObject({ Authorization: 'Bearer jwt' })
  })

  it('em StrictMode renova a sessão uma única vez (refresh é de uso único)', async () => {
    const fetchMock = mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
      ...withCouple,
    })
    render(
      <StrictMode>
        <App />
      </StrictMode>,
    )

    await screen.findByRole('heading', { name: 'Ana' })
    const refreshCalls = fetchMock.mock.calls.filter(([url]) => url === '/api/auth/refresh')
    expect(refreshCalls).toHaveLength(1)
  })
})
