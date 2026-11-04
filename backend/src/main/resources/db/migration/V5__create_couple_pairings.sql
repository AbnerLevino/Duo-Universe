-- Épico 2, Etapa 2: o convite (CouplePairing) como máquina de estados pura — ainda sem convite real por
-- @handle/link (Etapas 3 e 5) nem confirmação dos dois lados (Etapa 8).
CREATE TABLE couple_pairings (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    initiator_user_id BIGINT      NOT NULL REFERENCES users (id),
    invitee_user_id   BIGINT      REFERENCES users (id),
    channel           VARCHAR(10) NOT NULL,
    state             VARCHAR(10) NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at        TIMESTAMPTZ,
    couple_id         BIGINT REFERENCES couples (id)
);

CREATE INDEX couple_pairings_initiator_idx ON couple_pairings (initiator_user_id);
CREATE INDEX couple_pairings_invitee_idx ON couple_pairings (invitee_user_id);
