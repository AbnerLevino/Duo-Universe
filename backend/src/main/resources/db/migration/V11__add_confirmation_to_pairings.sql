-- Épico 2, Etapa 8 (RN-06/RN-08): cada pessoa confirma a sua parte do vínculo; só quando as duas confirmaram
-- é que o Couple nasce de verdade (ver CouplePairingServiceImpl.confirm / CoupleFactory). Pela ordem decidida
-- pelo fundador em 2026-11-05 (firmar primeiro, história e datas depois), o Couple nasce vazio.
ALTER TABLE couple_pairings
    ADD COLUMN initiator_confirmed_at TIMESTAMPTZ,
    ADD COLUMN invitee_confirmed_at   TIMESTAMPTZ;
