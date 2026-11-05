-- Épico 2, Etapa 6 (RN-25/RN-26): a história de cada pessoa sobre o vínculo, em 4 passos curtos.
-- Uma linha por (pairing_id, user_id): os 4 passos ficam em colunas, não em linhas separadas, porque
-- são sempre os mesmos 4 e sempre lidos/escritos juntos. Todas as colunas de texto começam nulas —
-- é assim que o rascunho existe "pela metade" enquanto a pessoa ainda está escrevendo (RN-25: só
-- precisa de texto, sem mínimo de caracteres; ver docs/epico-2-vinculo.md).
CREATE TABLE couple_pairing_stories (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pairing_id           BIGINT NOT NULL REFERENCES couple_pairings (id),
    user_id              BIGINT NOT NULL REFERENCES users (id),
    how_we_met           TEXT,
    time_before_dating   TEXT,
    what_i_thought_then  TEXT,
    what_i_think_now     TEXT
);

CREATE UNIQUE INDEX couple_pairing_stories_pairing_user_unique
    ON couple_pairing_stories (pairing_id, user_id);
