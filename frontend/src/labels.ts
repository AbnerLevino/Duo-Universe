import type { MaritalStatus, MediaVisibility, ProfileVisibility, ReportReason } from './api'

// Os nomes que a pessoa vê na tela ficam todos aqui: trocar "Duollowers" por outra palavra é mudar uma linha.
export const TERMS = { duoName: 'DuoName', followers: 'Duollowers', following: 'Duollowing' }

export const MARITAL_LABELS: Record<MaritalStatus, string> = {
  SINGLE: 'Solteiro(a)',
  DATING: 'Namorando',
  MARRIED: 'Casado(a)',
  DIVORCED: 'Divorciado(a)',
  WIDOWED: 'Viúvo(a)',
}
export const MARITAL_OPTIONS = Object.keys(MARITAL_LABELS) as MaritalStatus[]

export const PROFILE_VISIBILITY_LABELS: Record<ProfileVisibility, string> = {
  PUBLIC: 'Público',
  PRIVATE: 'Privado',
}

export const MEDIA_VISIBILITY_LABELS: Record<MediaVisibility, string> = {
  ONLY_ME: 'Só eu',
  DUOLLOWERS: TERMS.followers,
  PUBLIC: 'Público',
}
export const MEDIA_VISIBILITY_OPTIONS = Object.keys(MEDIA_VISIBILITY_LABELS) as MediaVisibility[]

export const REPORT_REASONS: { value: ReportReason; label: string }[] = [
  { value: 'NUDITY_OR_SEXUAL', label: 'Nudez ou conteúdo sexual' },
  { value: 'HARASSMENT_OR_THREAT', label: 'Assédio ou ameaça' },
  { value: 'FAKE_PROFILE', label: 'Perfil falso' },
  { value: 'SUSPECTED_MINOR', label: 'Suspeita de menor de idade' },
  { value: 'SPAM', label: 'Spam' },
  { value: 'OTHER', label: 'Outro motivo' },
]

export const BIO_MAX = 100
export const PROFESSION_MAX = 20
export const CAPTION_MAX = 200
