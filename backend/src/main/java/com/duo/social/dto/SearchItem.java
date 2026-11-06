package com.duo.social.dto;

/** Um resultado da busca: o cartão e a relação de quem buscou com essa pessoa (para o botão certo). */
public record SearchItem(ProfileCard profile, Relation relation) {
}
