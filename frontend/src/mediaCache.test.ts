import { clearSession, loadMediaUrl } from './session'
import { callsTo, mockBackend } from './testHelpers'

const image = () => new Response(new Blob(['x'], { type: 'image/png' }), { status: 200 })

describe('cache de fotos da sessão', () => {
  beforeEach(() => {
    clearSession()
    vi.mocked(URL.revokeObjectURL).mockClear()
    vi.mocked(URL.createObjectURL).mockClear()
  })
  afterEach(() => vi.unstubAllGlobals())

  it('baixa a mesma foto uma única vez, mesmo pedida duas vezes', async () => {
    const fetchMock = mockBackend({ 'GET /api/media/5': image })

    const [first, second] = await Promise.all([loadMediaUrl(5), loadMediaUrl(5)])

    expect(first).toBe('blob:foto-de-teste')
    expect(second).toBe(first)
    expect(callsTo(fetchMock, 'GET /api/media/5')).toHaveLength(1)
  })

  it('sem permissão (404) devolve null e não quebra', async () => {
    mockBackend({})

    await expect(loadMediaUrl(9)).resolves.toBeNull()
  })

  it('ao sair, esquece as fotos para a próxima pessoa nunca herdar uma que não pode ver', async () => {
    const fetchMock = mockBackend({ 'GET /api/media/5': image })
    await loadMediaUrl(5)

    clearSession()
    await Promise.resolve()

    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:foto-de-teste')
    await loadMediaUrl(5)
    expect(callsTo(fetchMock, 'GET /api/media/5')).toHaveLength(2)
  })
})
