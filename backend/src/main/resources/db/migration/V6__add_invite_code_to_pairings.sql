-- Épico 2, Etapa 3: convite por link (P1, alternativa ao @handle da Etapa 5). O código em si nunca é
-- guardado — só o hash (mesmo padrão de refresh_tokens.token_hash, RA-09).
ALTER TABLE couple_pairings ADD COLUMN invite_code_hash VARCHAR(64);

CREATE UNIQUE INDEX couple_pairings_invite_code_hash_unique
    ON couple_pairings (invite_code_hash) WHERE invite_code_hash IS NOT NULL;
