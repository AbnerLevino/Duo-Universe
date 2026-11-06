package com.duo.social.dto;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;

/**
 * O perfil como o visitante pode ver. {@code canSeeContent} diz se as fotos e momentos podem ser pedidos (a
 * lista de fotos ainda filtra foto por foto); {@code self} é verdadeiro quando o visitante é o próprio dono.
 */
public record PublicProfile(Long userId, String displayName, String handle, String bio, String profession,
		MaritalStatus maritalStatus, ProfileVisibility profileVisibility, Long avatarMediaId, long duollowers,
		long duollowing, Relation relation, boolean self, boolean canSeeContent) {
}
