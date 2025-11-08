package org.wemightmove.movemap.global.exception;

import lombok.Builder;

public record ErrorDto(
        String timestamp,
        int status,
        int code,
        String message,
        String path
) {
    @Builder
    public ErrorDto{}
}
