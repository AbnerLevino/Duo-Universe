// Formatos de dado usados por mais de uma feature (por isso moram em shared/, não dentro de uma feature só).
// Cada um está aqui porque pelo menos duas pastas de features/ diferentes importam o mesmo tipo.

export type Channel = 'HANDLE' | 'LINK'
export type PairingState = 'PENDING' | 'CONFIRMED' | 'CANCELLED'
export type CoupleState = 'ACTIVE' | 'PAUSED' | 'ENDED'

export type Pairing = {
  id: number
  initiatorUserId: number
  inviteeUserId: number | null
  channel: Channel
  state: PairingState
  expiresAt: string
  createdAt: string
  coupleId: number | null
  initiatorConfirmedAt: string | null
  inviteeConfirmedAt: string | null
  dateProposedByUserId: number | null
  proposedKnownSinceDate: string | null
  proposedDatingSinceDate: string | null
  datesAcceptedAt: string | null
}

export type Couple = {
  id: number
  state: CoupleState
  knownSinceDate: string | null
  datingSinceDate: string | null
  pausedAt: string | null
  endedAt: string | null
}

export type MaritalStatus = 'SINGLE' | 'DATING' | 'MARRIED' | 'DIVORCED' | 'WIDOWED'
export type ProfileVisibility = 'PUBLIC' | 'PRIVATE'
export type Relation = 'NONE' | 'REQUESTED' | 'FOLLOWING'
export type MediaVisibility = 'ONLY_ME' | 'DUOLLOWERS' | 'PUBLIC'
export type ReportReason =
  | 'NUDITY_OR_SEXUAL'
  | 'HARASSMENT_OR_THREAT'
  | 'FAKE_PROFILE'
  | 'SUSPECTED_MINOR'
  | 'SPAM'
  | 'OTHER'

export type MyProfile = {
  id: number
  displayName: string
  handle: string | null
  bio: string | null
  profession: string | null
  maritalStatus: MaritalStatus
  profileVisibility: ProfileVisibility
  avatarMediaId: number | null
  hidden: boolean
}

export type MediaSummary = {
  id: number
  ownerUserId: number
  kind: 'AVATAR' | 'PHOTO'
  visibility: MediaVisibility
  caption: string | null
  contentType: string
  sizeBytes: number
  createdAt: string
}
