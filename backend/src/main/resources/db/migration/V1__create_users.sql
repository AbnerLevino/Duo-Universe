-- Épico 1 (RA-01, RA-02): conta sem senha; e-mail único sem diferenciar maiúsculas.
CREATE TABLE users (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email             VARCHAR(320) NOT NULL,
    display_name      VARCHAR(100) NOT NULL,
    email_verified_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ
);

CREATE UNIQUE INDEX users_email_unique ON users (lower(email));
