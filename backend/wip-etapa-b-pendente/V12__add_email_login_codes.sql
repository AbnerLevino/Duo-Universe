-- Épico 1, Etapa B (RA-01, RA-03, RA-09): código de 6 dígitos por e-mail como forma de entrar sem senha,
-- antecipada a pedido do fundador (2026-11-05) para dar uma segunda forma de conta além do Google (DEC-030).
-- google_sub deixa de ser obrigatório: agora uma conta pode nascer só pelo e-mail, sem nunca passar pelo Google.
ALTER TABLE users ALTER COLUMN google_sub DROP NOT NULL;

CREATE TABLE email_login_codes (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email        VARCHAR(320)  NOT NULL,
    code_hash    VARCHAR(64)   NOT NULL,
    requested_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ   NOT NULL,
    attempts     INT           NOT NULL DEFAULT 0,
    consumed_at  TIMESTAMPTZ
);

-- Para achar rápido "o código mais recente deste e-mail" (confirmação) sem varrer a tabela inteira.
CREATE INDEX email_login_codes_email_requested_at ON email_login_codes (lower(email), requested_at DESC);
