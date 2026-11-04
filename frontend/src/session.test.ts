import { authedFetch, clearSession, refreshSession } from './session'

const jsonResponse = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status })

describe('session', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('renova uma vez e repete quando o token de acesso venceu', async () => {
    const tokens = ['velho', 'novo']
    const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
      if (input === '/api/auth/refresh') {
        return Promise.resolve(jsonResponse(200, { accessToken: tokens.shift() }))
      }
      const auth = (init?.headers as Record<string, string>).Authorization
      return Promise.resolve(
        auth === 'Bearer novo' ? jsonResponse(200, { ok: true }) : jsonResponse(401),
      )
    })
    vi.stubGlobal('fetch', fetchMock)

    expect(await refreshSession()).toBe('velho')
    const response = await authedFetch('/api/me')

    expect(response?.status).toBe(200)
    expect(fetchMock.mock.calls.filter(([url]) => url === '/api/auth/refresh')).toHaveLength(2)
  })

  it('devolve null quando a sessão acabou de vez', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse(401))),
    )

    expect(await authedFetch('/api/me')).toBeNull()
  })

  it('chamadas simultâneas de refresh dividem uma única requisição', async () => {
    const fetchMock = vi.fn(() => Promise.resolve(jsonResponse(200, { accessToken: 'jwt' })))
    vi.stubGlobal('fetch', fetchMock)

    await Promise.all([refreshSession(), refreshSession()])

    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
})
