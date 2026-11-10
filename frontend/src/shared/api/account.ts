// Escolher o @handle é a mesma ação de backend (PUT /api/me/handle) chamada em dois lugares diferentes:
// na primeira vez que a pessoa entra (features/auth/HandleSetupScreen) e ao editar o perfil depois
// (features/profile/EditProfileForm). Por isso fica aqui, em vez de dentro de uma das duas features.

import { call, withJsonBody } from './client'

export const chooseHandle = (handle: string) =>
  call<{ id: number; email: string; displayName: string; handle: string | null }>(
    '/api/me/handle',
    withJsonBody('PUT', { handle }),
  )
