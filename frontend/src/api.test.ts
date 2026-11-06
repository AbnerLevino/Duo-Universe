import { ApiError, getPairing, myCouple, saveStoryStep } from './api'
import { clearSession, refreshSession } from './session'

const jsonResponse = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status })

describe('api', () => {
  beforeEach(async () => {
    clearSession()
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse(200, { accessToken: 'jwt' }))),
    )
    await refreshSession()
  })
  afterEach(() => vi.unstubAllGlobals())

  it('devolve o corpo em JSON quando a resposta é 200', async () => {
    const pairing = { id: 7, state: 'PENDING' }
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse(200, pairing))),
    )

    expect(await getPairing(7)).toEqual(pairing)
  })

  it('devolve null quando a resposta é 204 (ex.: sem casal ainda)', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse(204))),
    )

    expect(await myCouple()).toBeNull()
  })

  it('traduz uma resposta de erro (ProblemDetail) em ApiError, com o detail do backend', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve(jsonResponse(409, { detail: 'O vínculo ainda não foi firmado.' })),
      ),
    )

    await expect(saveStoryStep(7, 'HOW_WE_MET', 'oi')).rejects.toMatchObject({
      message: 'O vínculo ainda não foi firmado.',
      status: 409,
    })
  })

  it('erro sem corpo de ProblemDetail cai numa mensagem genérica, sem quebrar', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse(500))),
    )

    await expect(getPairing(7)).rejects.toBeInstanceOf(ApiError)
  })
})
