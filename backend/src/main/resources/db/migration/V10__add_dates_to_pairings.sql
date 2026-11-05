-- Épico 2, Etapa 7 (P3/RN-27): as duas datas do casal (quando se conheceram, quando começaram a namorar) só
-- nascem por consenso -- um propõe, o outro aceita (ou propõe outras, até concordarem). Essas colunas guardam
-- a negociação; quando o Couple nascer (Etapa 8), os valores aceitos são copiados para couples.*_since_date.
ALTER TABLE couple_pairings
    ADD COLUMN date_proposed_by_user_id   BIGINT REFERENCES users (id),
    ADD COLUMN proposed_known_since_date  DATE,
    ADD COLUMN proposed_dating_since_date DATE,
    ADD COLUMN dates_accepted_at          TIMESTAMPTZ;
