// O "motor" genérico de chamada HTTP. Sem regra de negócio nenhuma aqui — quem decide o caminho, o verbo e
// o corpo é cada api.ts de feature; este arquivo só sabe como falar com o backend e traduzir erro.

import { authedFetch } from '../session'

/** O que o GlobalExceptionHandler do backend devolve (ProblemDetail) quando uma regra de negócio barra a ação. */
export class ApiError extends Error {
  status: number

  constructor(status: number, detail: string) {
    super(detail)
    this.status = status
  }
}

export async function call<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await authedFetch(path, init)
  if (!response) throw new ApiError(401, 'Sessão encerrada. Entre de novo.')
  if (response.status === 204) return null as T
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null
    throw new ApiError(response.status, problem?.detail ?? 'Não foi possível completar a ação.')
  }
  return (await response.json()) as T
}

export const withJsonBody = (method: string, body: unknown): RequestInit => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

export function fileForm(blob: Blob, extra: Record<string, string> = {}) {
  const form = new FormData()
  form.append('file', blob, 'imagem.jpg')
  for (const [key, value] of Object.entries(extra)) form.append(key, value)
  return form
}

/** Escapa um @handle pra ir seguro dentro de uma URL (ex.: `/api/profiles/${handlePath(handle)}`). */
export const handlePath = (handle: string) => encodeURIComponent(handle)

/** A mensagem que o backend mandou (ProblemDetail) ou, se for outro erro, uma frase padrão. */
export const errorMessage = (err: unknown, fallback: string) =>
  err instanceof ApiError ? err.message : fallback
