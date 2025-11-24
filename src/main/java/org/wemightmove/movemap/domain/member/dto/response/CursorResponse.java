package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        boolean hasNext,
        Long lastId
) {

    public static <T> CursorResponse<T> empty() {
        return new CursorResponse<>(List.of(), false, null);
    }

    public static <T> CursorResponse<T> of(
            List<T> content,
            boolean hasNext,
            java.util.function.Function<T, Long> idExtractor
    ) {
        Long lastId = content.isEmpty() ? null : idExtractor.apply(content.get(content.size() - 1));
        return new CursorResponse<>(content, hasNext, lastId);
    }
}
