import { fireEvent, screen } from '@testing-library/react'
import EditProfileForm from './EditProfileForm'
import { clearSession, type User } from './session'
import { callsTo, jsonResponse, mockBackend, renderAt } from './testHelpers'

const USER: User = { id: 1, email: 'ana@example.com', displayName: 'Ana', handle: 'ana' }
const PROFILE = {
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

function open(onUserChanged = vi.fn(), onProfileChanged = vi.fn()) {
  renderAt(
    '/perfil/editar',
    '/perfil/editar',
    <EditProfileForm
      user={USER}
      onUserChanged={onUserChanged}
      onLogout={vi.fn()}
      onLogoutAll={vi.fn()}
      onProfileChanged={onProfileChanged}
      onClose={vi.fn()}
    />,
  )
  return onUserChanged
}

describe('EditProfileForm', () => {
  beforeEach(() => clearSession())
  afterEach(() => vi.unstubAllGlobals())

  it('carrega os dados atuais e conta os caracteres da bio', async () => {
    mockBackend({ 'GET /api/me/profile': () => jsonResponse(200, PROFILE) })
    open()

    expect(await screen.findByLabelText(/Bio \(13\/100\)/)).toHaveValue('Gosto de café')
    expect(screen.getByLabelText('Profissão')).toHaveValue('Dev')
    expect(screen.getByLabelText('Como você está?')).toHaveValue('SINGLE')
    expect(screen.getByLabelText('Quem vê os seus momentos')).toHaveValue('PUBLIC')
  })

  it('oferece os cinco estados civis', async () => {
    mockBackend({ 'GET /api/me/profile': () => jsonResponse(200, PROFILE) })
    open()

    const select = await screen.findByLabelText('Como você está?')

    expect(Array.from(select.querySelectorAll('option')).map((o) => o.textContent)).toEqual([
      'Solteiro(a)',
      'Namorando',
      'Casado(a)',
      'Divorciado(a)',
      'Viúvo(a)',
    ])
  })

  it('salvar envia todos os campos e avisa o app do novo nome', async () => {
    const fetchMock = mockBackend({
      'GET /api/me/profile': () => jsonResponse(200, PROFILE),
      'PUT /api/me/profile': () =>
        jsonResponse(200, { ...PROFILE, displayName: 'Ana Clara', maritalStatus: 'DATING' }),
    })
    const onUserChanged = open()

    fireEvent.change(await screen.findByLabelText('Nome'), { target: { value: 'Ana Clara' } })
    fireEvent.change(screen.getByLabelText('Como você está?'), { target: { value: 'DATING' } })
    fireEvent.change(screen.getByLabelText('Quem vê os seus momentos'), {
      target: { value: 'PRIVATE' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Perfil salvo.')
    const [, init] = callsTo(fetchMock, 'PUT /api/me/profile')[0]
    expect(JSON.parse(String(init?.body))).toEqual({
      displayName: 'Ana Clara',
      bio: 'Gosto de café',
      profession: 'Dev',
      maritalStatus: 'DATING',
      profileVisibility: 'PRIVATE',
    })
    expect(onUserChanged).toHaveBeenCalledWith({ ...USER, displayName: 'Ana Clara' })
  })

  it('mostra o motivo quando o backend recusa o perfil', async () => {
    mockBackend({
      'GET /api/me/profile': () => jsonResponse(200, PROFILE),
      'PUT /api/me/profile': () =>
        jsonResponse(400, { detail: 'O nome deve ter de 2 a 50 caracteres' }),
    })
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('O nome deve ter de 2 a 50')
  })

  it('o botão de trocar DuoName só vale para um nome novo e válido', async () => {
    mockBackend({ 'GET /api/me/profile': () => jsonResponse(200, PROFILE) })
    open()
    const button = await screen.findByRole('button', { name: 'Trocar DuoName' })

    expect(button).toBeDisabled() // o mesmo nome que já tem
    fireEvent.change(screen.getByLabelText('DuoName'), { target: { value: 'a!' } })
    expect(button).toBeDisabled() // formato inválido
    fireEvent.change(screen.getByLabelText('DuoName'), { target: { value: 'ana_nova' } })
    expect(button).toBeEnabled()
  })

  it('trocar o DuoName chama o backend; no limite diário mostra a mensagem dele', async () => {
    mockBackend({
      'GET /api/me/profile': () => jsonResponse(200, PROFILE),
      'PUT /api/me/handle': () =>
        jsonResponse(429, { detail: 'Você já trocou o DuoName 2 vezes hoje. Tente amanhã.' }),
    })
    open()

    fireEvent.change(await screen.findByLabelText('DuoName'), { target: { value: 'ana_nova' } })
    fireEvent.click(screen.getByRole('button', { name: 'Trocar DuoName' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('2 vezes hoje')
  })

  it('remover a foto pede confirmação antes', async () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false)
    const fetchMock = mockBackend({
      'GET /api/me/profile': () => jsonResponse(200, { ...PROFILE, avatarMediaId: 4 }),
      'GET /api/media/4': () => new Response(new Blob(['x'], { type: 'image/png' })),
    })
    open()

    fireEvent.click(await screen.findByRole('button', { name: 'Remover foto' }))

    expect(confirm).toHaveBeenCalled()
    expect(callsTo(fetchMock, 'DELETE /api/me/avatar')).toHaveLength(0)
    confirm.mockRestore()
  })
})
