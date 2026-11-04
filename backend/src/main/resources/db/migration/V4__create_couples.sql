-- Épico 2 (RN-06, RN-07, RN-08): só o esqueleto do casal. Nenhuma linha nasce aqui ainda —
-- isso só acontece na Etapa 8, quando as duas pessoas confirmam o vínculo.
CREATE TABLE couples (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    state             VARCHAR(10)  NOT NULL,
    known_since_date  DATE,
    dating_since_date DATE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    paused_at         TIMESTAMPTZ,
    ended_at          TIMESTAMPTZ
);

CREATE TABLE couple_members (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    couple_id  BIGINT      NOT NULL REFERENCES couples (id),
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at    TIMESTAMPTZ
);

-- RN-06: uma pessoa só pode estar num vínculo ACTIVE ou PAUSED por vez. "left_at IS NULL" é o sinal de
-- "esta pessoa ainda está dentro deste vínculo" — o índice único só vale para essas linhas (índice parcial).
CREATE UNIQUE INDEX couple_members_active_person_unique
    ON couple_members (user_id) WHERE left_at IS NULL;

CREATE INDEX couple_members_couple_id_idx ON couple_members (couple_id);
