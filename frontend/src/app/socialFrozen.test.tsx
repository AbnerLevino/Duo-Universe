import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
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
  profileVisibility: 'PRIVATE',
  avatarMediaId: null,
  hidden: false,
}

/** RF-A11: com a camada social congelada, a moldura não mostra busca, Duollowers nem Duollowing. */
describe('camada social congelada', () => {
  afterEach(() => {
    clearSession()
    vi.unstubAllGlobals()
    vi.unstubAllEnvs()
  })

  it('não mostra Buscar nem os números de Duollowers e Duollowing, e nem pede esses dados', async () => {
    vi.stubEnv('VITE_FEATURE_SOCIAL', 'false')
    const fetchMock = mockBackend({
      'GET /api/me/profile': () => jsonResponse(200, PROFILE),
    })

    render(
      <MemoryRouter initialEntries={['/inicio']}>
        <AppLayout
          user={USER}
          onUserChanged={() => {}}
          onLogout={() => {}}
          onLogoutAll={() => {}}
        />
      </MemoryRouter>,
    )

    await waitFor(() => expect(screen.getByRole('navigation')).toBeInTheDocument())
    expect(screen.queryByRole('link', { name: /Buscar/ })).not.toBeInTheDocument()
    expect(screen.queryByText(/Duollowers/)).not.toBeInTheDocument()
    expect(screen.queryByText(/Duollowing/)).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Vínculo/ })).toBeInTheDocument()
    const asked = fetchMock.mock.calls.map(([url]) => String(url))
    expect(asked).not.toContain('/api/me/follow-requests')
    expect(asked).not.toContain('/api/profiles/ana')
  })
})
