-- Épico 3, Etapa 2 (perfil do Personal). Bio de até 100 caracteres, profissão de até 20, estado civil e
-- visibilidade do perfil (o Personal nasce PÚBLICO — decisão do fundador, 2026-11-06).
-- search_name: o nome de exibição sem acento e em minúsculas, para a busca por nome achar "joao" em "João"
-- (Etapa 4). É calculado no código Java a cada mudança de nome; aqui só preenchemos quem já existe.
-- handle_changes_*: contam quantas vezes o @handle foi trocado hoje (limite de 2 por dia).
-- hidden_at: moderação manual (Etapa 6) — um perfil oculto some da busca e das telas dos outros.
-- avatar_media_id: a foto de perfil; a chave estrangeira entra na migration das mídias (Etapa 7).
ALTER TABLE users
    ADD COLUMN bio                  VARCHAR(100),
    ADD COLUMN profession           VARCHAR(20),
    ADD COLUMN marital_status       VARCHAR(20)  NOT NULL DEFAULT 'SINGLE',
    ADD COLUMN profile_visibility   VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    ADD COLUMN search_name          VARCHAR(100),
    ADD COLUMN handle_changes_on    DATE,
    ADD COLUMN handle_changes_today INT          NOT NULL DEFAULT 0,
    ADD COLUMN hidden_at            TIMESTAMPTZ,
    ADD COLUMN avatar_media_id      BIGINT;

UPDATE users
SET search_name = translate(lower(display_name),
                            'áàâãäéèêëíìîïóòôõöúùûüçñ',
                            'aaaaaeeeeiiiiooooouuuucn');

ALTER TABLE users ALTER COLUMN search_name SET NOT NULL;

CREATE INDEX users_search_name_idx ON users (search_name);
