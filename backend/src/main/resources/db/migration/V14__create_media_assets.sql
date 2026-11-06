-- Épico 3, Etapas 7 e 9: o registro de cada foto (o arquivo em si fica no armazenamento, atrás do
-- MediaStorageProvider). Uma linha por arquivo: de quem é, onde mora (scope), quem pode ver (visibility),
-- onde está guardado (storage_key) e o checksum SHA-256 do conteúdo (RN-19, RN-23).
-- kind: AVATAR (foto de perfil) ou PHOTO (momento do Personal). hidden_at: moderação manual (Etapa 6).
CREATE TABLE media_assets (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_user_id BIGINT       NOT NULL REFERENCES users (id),
    kind          VARCHAR(20)  NOT NULL,
    scope         VARCHAR(20)  NOT NULL,
    visibility    VARCHAR(20)  NOT NULL,
    storage_key   VARCHAR(200) NOT NULL,
    content_type  VARCHAR(50)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    sha256        CHAR(64)     NOT NULL,
    caption       VARCHAR(200),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    hidden_at     TIMESTAMPTZ
);

CREATE UNIQUE INDEX media_assets_storage_key_unique ON media_assets (storage_key);
CREATE INDEX media_assets_owner_idx ON media_assets (owner_user_id, kind, created_at DESC);

-- A foto de perfil de users aponta para um desses registros.
ALTER TABLE users
    ADD CONSTRAINT users_avatar_media_fk FOREIGN KEY (avatar_media_id) REFERENCES media_assets (id)
        ON DELETE SET NULL;
