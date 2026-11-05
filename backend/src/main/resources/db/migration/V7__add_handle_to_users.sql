-- Épico 2, Etapa 4: @handle único (RN-30) para buscar alguém dentro do próprio Duo e convidar para o
-- vínculo. Nullable porque quem já tinha conta (Épico 1) ainda não escolheu o seu.
ALTER TABLE users ADD COLUMN handle VARCHAR(20);

CREATE UNIQUE INDEX users_handle_unique ON users (lower(handle)) WHERE handle IS NOT NULL;
