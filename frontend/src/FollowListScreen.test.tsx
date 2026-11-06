import { fireEvent, screen } from '@testing-library/react'
import FollowListScreen from './FollowListScreen'
import { clearSession } from './session'
import { callsTo, jsonResponse, mockBackend, renderAt } from './testHelpers'

const PEDRO = {
  userId: 8,
  displayName: 'Pedro Lima',
  handle: 'pedro',
  bio: null,
  profession: null,
  avatarMediaId: null,
}

describe('FollowListScreen', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('aceitar um pedido chama o backend e atualiza a lista', async () => {
    let requests = [PEDRO]
    const fetchMock = mockBackend({
      'GET /api/me/follow-requests': () => jsonResponse(200, requests),
      'POST /api/me/follow-requests/pedro/accept': () => {
        requests = []
        return jsonResponse(204)
      },
    })
    renderAt('/lista/:kind', '/lista/pedidos', <FollowListScreen />)

    expect(await screen.findByText('Pedro Lima')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Aceitar' }))

    expect(await screen.findByText('Nenhum pedido no momento.')).toBeInTheDocument()
    expect(callsTo(fetchMock, 'POST /api/me/follow-requests/pedro/accept')).toHaveLength(1)
  })

  it('recusar usa a rota de recusa, sem avisar a outra pessoa', async () => {
    const fetchMock = mockBackend({
      'GET /api/me/follow-requests': () => jsonResponse(200, [PEDRO]),
      'DELETE /api/me/follow-requests/pedro': () => jsonResponse(204),
    })
    renderAt('/lista/:kind', '/lista/pedidos', <FollowListScreen />)

    fireEvent.click(await screen.findByRole('button', { name: 'Recusar' }))

    await screen.findByText('Pedro Lima')
    expect(callsTo(fetchMock, 'DELETE /api/me/follow-requests/pedro')).toHaveLength(1)
  })

  it('a lista de Duollowers permite remover alguém', async () => {
    const fetchMock = mockBackend({
      'GET /api/me/duollowers': () => jsonResponse(200, [PEDRO]),
      'DELETE /api/me/duollowers/pedro': () => jsonResponse(204),
    })
    renderAt('/lista/:kind', '/lista/duollowers', <FollowListScreen />)

    fireEvent.click(await screen.findByRole('button', { name: 'Remover' }))

    await screen.findByText('Pedro Lima')
    expect(callsTo(fetchMock, 'DELETE /api/me/duollowers/pedro')).toHaveLength(1)
  })

  it('a lista de bloqueados permite desbloquear', async () => {
    const fetchMock = mockBackend({
      'GET /api/me/blocks': () => jsonResponse(200, [PEDRO]),
      'DELETE /api/me/blocks/pedro': () => jsonResponse(204),
    })
    renderAt('/lista/:kind', '/lista/bloqueados', <FollowListScreen />)

    fireEvent.click(await screen.findByRole('button', { name: 'Desbloquear' }))

    await screen.findByText('Pedro Lima')
    expect(callsTo(fetchMock, 'DELETE /api/me/blocks/pedro')).toHaveLength(1)
  })

  it('uma lista que não existe mostra um aviso', async () => {
    mockBackend({})
    renderAt('/lista/:kind', '/lista/qualquercoisa', <FollowListScreen />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Lista não encontrada')
  })
})
