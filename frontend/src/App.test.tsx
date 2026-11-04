import { fireEvent, render, screen } from '@testing-library/react'
import { StrictMode } from 'react'
import App from './App'
import { clearSession } from './session'

const jsonResponse = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status })

const ME = { id: 1, email: 'ana@example.com', displayName: 'Ana' }
const TOKEN = { accessToken: 'jwt', expiresInSeconds: 900 }

/** Simula o backend: o que cada rota responde. */
function mockBackend(routes: Record<string, () => Response>) {
  const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
    const key = `${init?.method ?? 'GET'} ${String(input)}`
    const handler = routes[key]
    return Promise.resolve(handler ? handler() : jsonResponse(404))
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
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

  it('com sessão mostra o nome da pessoa', async () => {
    mockBackend({
      'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
      'GET /api/me': () => jsonResponse(200, ME),
    })
    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Ana' })).toBeInTheDocument()
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
