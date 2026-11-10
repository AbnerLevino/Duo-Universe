// Vínculo: convite, confirmação, história em 4 passos, datas, pausa/saída (Épico 2).

import { call, withJsonBody } from '../../shared/api/client'
import type { Couple, Pairing } from '../../shared/api/types'

export type StoryStep =
  'HOW_WE_MET' | 'TIME_BEFORE_DATING' | 'WHAT_I_THOUGHT_THEN' | 'WHAT_I_THINK_NOW'

export const STORY_STEPS: StoryStep[] = [
  'HOW_WE_MET',
  'TIME_BEFORE_DATING',
  'WHAT_I_THOUGHT_THEN',
  'WHAT_I_THINK_NOW',
]

export type Story = {
  pairingId: number
  userId: number
  howWeMet: string | null
  timeBeforeDating: string | null
  whatIThoughtThen: string | null
  whatIThinkNow: string | null
}

export type IssuedInvite = { pairingId: number; code: string; expiresAt: string }

/** O casal de quem está logado, ou null se ainda não tem vínculo. */
export const myCouple = () => call<Couple | null>('/api/couples/mine')
/** Pausa o casal (RN-13): ninguém mais vê o centro até alguém retomar. */
export const pauseCouple = (id: number) =>
  call<Couple>(`/api/couples/${id}/pause`, { method: 'POST' })
/** Retoma um casal pausado. */
export const resumeCouple = (id: number) =>
  call<Couple>(`/api/couples/${id}/resume`, { method: 'POST' })
/** Sai do casal (RN-14): no MVP de 2 membros, termina o vínculo pros dois. */
export const leaveCouple = (id: number) =>
  call<void>(`/api/couples/${id}/leave`, { method: 'POST' })

/** Os convites/vínculos de quem está logado (pendentes, confirmados ou cancelados). */
export const myPairings = () => call<Pairing[]>('/api/pairings/mine')
export const getPairing = (id: number) => call<Pairing>(`/api/pairings/${id}`)

/** Convida alguém pelo @handle exato. */
export const inviteByHandle = (handle: string) =>
  call<Pairing>('/api/pairings/invite-by-handle', withJsonBody('POST', { handle }))

/** Gera um link de convite (código de 7 dias). */
export const inviteByLink = () =>
  call<IssuedInvite>('/api/pairings/invite-by-link', { method: 'POST' })

/** Resgata um convite por link, usando o código. */
export const redeemByLink = (code: string) =>
  call<Pairing>('/api/pairings/redeem', withJsonBody('POST', { code }))

/** Confirma o convite (RN-08: o Couple só nasce quando os dois confirmam). */
export const confirmPairing = (id: number) =>
  call<Pairing>(`/api/pairings/${id}/confirm`, { method: 'POST' })
export const cancelPairing = (id: number) =>
  call<void>(`/api/pairings/${id}/cancel`, { method: 'POST' })

/** Salva um passo da história (RN-25); cada passo salva sozinho, sem mínimo de caracteres. */
export const saveStoryStep = (pairingId: number, step: StoryStep, text: string) =>
  call<void>(`/api/pairings/${pairingId}/story/${step}`, withJsonBody('PUT', { text }))

/** A história do parceiro — só vem quando as duas estiverem completas (RN-26). */
export const partnerStory = (pairingId: number) =>
  call<Story | null>(`/api/pairings/${pairingId}/partner-story`)

/** Propõe a data de quando se conheceram e a de quando começaram a namorar (RN-27). */
export const proposeDates = (pairingId: number, knownSinceDate: string, datingSinceDate: string) =>
  call<void>(
    `/api/pairings/${pairingId}/dates`,
    withJsonBody('PUT', { knownSinceDate, datingSinceDate }),
  )

/** Aceita as datas propostas pelo parceiro. */
export const acceptDates = (pairingId: number) =>
  call<void>(`/api/pairings/${pairingId}/dates/accept`, { method: 'POST' })
