import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import AppLayout from './AppLayout'
import { clearSession, type User } from '@/shared/session'
import { jsonResponse, mockBackend } from '@/shared/test/testHelpers'

const USER: User = { id: 1, email: 'ana@example.com', displayName: 'Ana', handle: 'ana' }
const PROFILE = {
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
const COUNTS = {
  userId: 1,
  displayName: 'Ana',
  handle: 'ana',
  bio: null,
  profession: null,
  maritalStatus: 'SINGLE',
  profileVisibility: 'PUBLIC',
  avatarMediaId: null,
  duollowers: 12,
  duollowing: 5,
  relation: 'NONE',
  self: true,
  canSeeContent: true,
}

const backend = () =>
  mockBackend({
    'GET /api/me/profile': () => jsonResponse(200, PROFILE),
    'GET /api/profiles/ana': () => jsonResponse(200, COUNTS),
    'GET /api/me/follow-requests': () => jsonResponse(200, []),
  })

/** A moldura com duas telas-filhas de mentira, para ver que só a área da direita troca. */
function open(user: User = USER) {
  return render(
    <MemoryRouter initialEntries={['/buscar']}>
      <Routes>
        <Route
          element={
            <AppLayout
              user={user}
              onUserChanged={vi.fn()}
              onLogout={vi.fn()}
              onLogoutAll={vi.fn()}
            />
          }
        >
          <Route path="/buscar" element={<p>Tela de busca</p>} />
          <Route path="/inicio" element={<p>Tela inicial</p>} />
        </Route>
        <Route path="/vinculo/handle" element={<p>Escolher DuoName</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('AppLayout', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('mostra a tela da rota ao lado do painel, com os números e o Editar', async () => {
    backend()
    open()

    expect(await screen.findByText('Tela de busca')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: /12\s*Duollowers/ })).toHaveAttribute(
      'href',
      '/lista/duollowers',
    )
    expect(screen.getByRole('link', { name: /5\s*Duollowing/ })).toHaveAttribute(
      'href',
      '/lista/duollowing',
    )
    expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument()
  })

  it('tem a navegação principal com as quatro opções', async () => {
    backend()
    open()

    const nav = await screen.findByRole('navigation', { name: 'Navegação principal' })

    expect(nav).toHaveTextContent('Início')
    expect(screen.getByRole('link', { name: 'Início' })).toHaveAttribute('href', '/inicio')
    expect(screen.getByRole('link', { name: 'Buscar' })).toHaveAttribute('href', '/buscar')
    expect(screen.getByRole('link', { name: 'Vínculo' })).toHaveAttribute(
      'href',
      '/vinculo/convidar',
    )
    expect(screen.getByRole('button', { name: 'Perfil' })).toBeInTheDocument()
  })

  it('os ícones são decorativos: leitores de tela não os repetem', async () => {
    backend()
    open()

    const nav = await screen.findByRole('navigation', { name: 'Navegação principal' })

    const icons = nav.querySelectorAll('svg')
    expect(icons).toHaveLength(4)
    icons.forEach((icon) => expect(icon).toHaveAttribute('aria-hidden', 'true'))
  })

  it('a página atual fica marcada e navegar troca só a área da direita', async () => {
    backend()
    open()

    expect(await screen.findByRole('link', { name: 'Buscar' })).toHaveClass('active')
    fireEvent.click(screen.getByRole('link', { name: 'Início' }))

    expect(await screen.findByText('Tela inicial')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Início' })).toHaveClass('active')
    expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument()
  })

  it('"Editar" e "Perfil" abrem o modal de editar perfil por cima da tela atual', async () => {
    backend()
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Editar' }))

    expect(await screen.findByRole('dialog', { name: 'Editar perfil' })).toBeInTheDocument()
    expect(screen.getByText('Tela de busca')).toBeInTheDocument()

    fireEvent.keyDown(document, { key: 'Escape' })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Perfil' }))
    expect(await screen.findByRole('dialog', { name: 'Editar perfil' })).toBeInTheDocument()
  })

  it('sem DuoName escolhido manda para a tela de escolher um', async () => {
    backend()
    open({ ...USER, handle: null })

    expect(await screen.findByText('Escolher DuoName')).toBeInTheDocument()
  })

  it('se o perfil não carregar o painel continua de pé e a tela da direita funciona', async () => {
    mockBackend({})
    open()

    expect(await screen.findByText('Tela de busca')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument()
  })
})
