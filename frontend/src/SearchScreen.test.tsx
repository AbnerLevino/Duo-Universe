import { fireEvent, screen } from '@testing-library/react'
import SearchScreen from './SearchScreen'
import { clearSession } from './session'
import { callsTo, jsonResponse, mockBackend, renderAt } from './testHelpers'

const JOAO = {
  userId: 7,
  displayName: 'João da Silva',
  handle: 'joao',
  bio: null,
  profession: 'Designer',
  avatarMediaId: null,
}

describe('SearchScreen', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('com menos de 3 letras explica e não chama a busca', async () => {
    const fetchMock = mockBackend({})
    renderAt('/buscar', '/buscar', <SearchScreen />)

    fireEvent.change(screen.getByLabelText(/Nome ou DuoName/), { target: { value: 'jo' } })
    await new Promise((resolve) => setTimeout(resolve, 450))

    expect(screen.getByText(/pelo menos 3 letras/)).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('/api/search'))).toBe(false)
  })

  it('com 3 letras ou mais busca e mostra as pessoas, e seguir troca o botão', async () => {
    const fetchMock = mockBackend({
      'GET /api/search/profiles?q=joao&page=0': () =>
        jsonResponse(200, { items: [{ profile: JOAO, relation: 'NONE' }], hasMore: false }),
      'POST /api/profiles/joao/follow': () => jsonResponse(200, { relation: 'FOLLOWING' }),
    })
    renderAt('/buscar', '/buscar', <SearchScreen />)

    fireEvent.change(screen.getByLabelText(/Nome ou DuoName/), { target: { value: 'joao' } })

    expect(await screen.findByText('João da Silva')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /João da Silva/ })).toHaveAttribute('href', '/u/joao')
    fireEvent.click(screen.getByRole('button', { name: 'Seguir' }))
    expect(await screen.findByRole('button', { name: 'Seguindo' })).toBeInTheDocument()
    expect(callsTo(fetchMock, 'POST /api/profiles/joao/follow')).toHaveLength(1)
  })

  it('mostra quando ninguém foi encontrado', async () => {
    mockBackend({
      'GET /api/search/profiles?q=zzzz&page=0': () =>
        jsonResponse(200, { items: [], hasMore: false }),
    })
    renderAt('/buscar', '/buscar', <SearchScreen />)

    fireEvent.change(screen.getByLabelText(/Nome ou DuoName/), { target: { value: 'zzzz' } })

    expect(await screen.findByText('Ninguém encontrado.')).toBeInTheDocument()
  })

  it('pedido de seguir aparece como "Pedido enviado" e permite cancelar', async () => {
    mockBackend({
      'GET /api/search/profiles?q=joao&page=0': () =>
        jsonResponse(200, { items: [{ profile: JOAO, relation: 'REQUESTED' }], hasMore: false }),
      'DELETE /api/profiles/joao/follow': () => jsonResponse(204),
    })
    renderAt('/buscar', '/buscar', <SearchScreen />)
    fireEvent.change(screen.getByLabelText(/Nome ou DuoName/), { target: { value: 'joao' } })

    fireEvent.click(await screen.findByRole('button', { name: /Pedido enviado/ }))

    expect(await screen.findByRole('button', { name: 'Seguir' })).toBeInTheDocument()
  })
})
