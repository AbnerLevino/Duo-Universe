-- RA-08: limite de tentativas por período, guardado no banco (sobrevive a reinício do servidor).
-- Uma linha por (scope, attempt_key); scope identifica a rota (ex.: "pairing_invite_by_handle"),
-- attempt_key identifica quem está tentando (hoje, o id da conta; o IP entra quando a rota tiver um
-- controller HTTP, Etapa 13).
CREATE TABLE rate_limit_attempts (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    scope              VARCHAR(50)  NOT NULL,
    attempt_key        VARCHAR(255) NOT NULL,
    window_started_at  TIMESTAMPTZ  NOT NULL,
    attempt_count      INT          NOT NULL
);

CREATE UNIQUE INDEX rate_limit_attempts_scope_key_unique ON rate_limit_attempts (scope, attempt_key);
