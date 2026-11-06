-- Épico 3, Etapas 5 e 6: Duollowers (seguir, com pedido quando o perfil é privado), bloqueio e denúncia.

-- follower_id segue followee_id. PENDING = pedido que o dono do perfil privado ainda não respondeu;
-- ACCEPTED = já segue. Recusar ou deixar de seguir apaga a linha (sem aviso à outra pessoa).
CREATE TABLE follows (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    follower_id  BIGINT      NOT NULL REFERENCES users (id),
    followee_id  BIGINT      NOT NULL REFERENCES users (id),
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at TIMESTAMPTZ,
    CONSTRAINT follows_not_self CHECK (follower_id <> followee_id),
    CONSTRAINT follows_unique UNIQUE (follower_id, followee_id)
);

CREATE INDEX follows_followee_idx ON follows (followee_id, status);

-- blocker_id bloqueou blocked_id. Vale nos dois sentidos para sumir da busca e do perfil.
CREATE TABLE blocks (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    blocker_id BIGINT      NOT NULL REFERENCES users (id),
    blocked_id BIGINT      NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT blocks_not_self CHECK (blocker_id <> blocked_id),
    CONSTRAINT blocks_unique UNIQUE (blocker_id, blocked_id)
);

CREATE INDEX blocks_blocked_idx ON blocks (blocked_id);

-- Denúncia de um perfil (target_media_id nulo) ou de uma foto. Sem painel no MVP: a revisão é manual, direto
-- no banco. O mesmo denunciante não repete a mesma denúncia.
CREATE TABLE reports (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reporter_id     BIGINT       NOT NULL REFERENCES users (id),
    target_type     VARCHAR(20)  NOT NULL,
    target_user_id  BIGINT       NOT NULL REFERENCES users (id),
    target_media_id BIGINT,
    reason          VARCHAR(30)  NOT NULL,
    details         VARCHAR(300),
    status          VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX reports_no_duplicate
    ON reports (reporter_id, target_type, target_user_id, COALESCE(target_media_id, 0));
