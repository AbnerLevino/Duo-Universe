package com.duo.social.dto;

import java.util.List;

public record SearchPage(List<SearchItem> items, boolean hasMore) {
}
