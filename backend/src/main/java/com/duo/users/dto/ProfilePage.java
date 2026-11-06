package com.duo.users.dto;

import java.util.List;

/** Uma página de resultados da busca; {@code hasMore} diz se existe uma página seguinte. */
public record ProfilePage(List<UserProfile> items, boolean hasMore) {
}
