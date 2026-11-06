import { render } from '@testing-library/react'
import type { ReactElement } from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

// Ferramentas só dos testes: um "backend de mentira" por rota e um jeito de abrir uma tela numa rota.

export const jsonResponse = (status: number, body?: unknown) =>
  new Response(body === undefined ? null : JSON.stringify(body), { status })

export const TOKEN = { accessToken: 'jwt', expiresInSeconds: 900 }

/** Cada chave é "MÉTODO /caminho"; rota não prevista responde 404. Já inclui a renovação da sessão. */
export function mockBackend(routes: Record<string, () => Response>) {
  const all: Record<string, () => Response> = {
    'POST /api/auth/refresh': () => jsonResponse(200, TOKEN),
    ...routes,
  }
  const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
    const handler = all[`${init?.method ?? 'GET'} ${String(input)}`]
    return Promise.resolve(handler ? handler() : jsonResponse(404))
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

/** Quantas vezes a rota foi chamada ("GET /api/media/users/7/photos"). */
export const callsTo = (fetchMock: ReturnType<typeof mockBackend>, key: string) =>
  fetchMock.mock.calls.filter(([url, init]) => `${init?.method ?? 'GET'} ${String(url)}` === key)

/** Abre `element` na rota `pattern`, já navegando para `path`; `others` são rotas vizinhas para ver o destino. */
export function renderAt(
  pattern: string,
  path: string,
  element: ReactElement,
  others: Record<string, ReactElement> = {},
) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path={pattern} element={element} />
        {Object.entries(others).map(([otherPath, other]) => (
          <Route key={otherPath} path={otherPath} element={other} />
        ))}
      </Routes>
    </MemoryRouter>,
  )
}
