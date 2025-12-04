package org.wemightmove.movemap.global.exception;

import lombok.Getter;

@Getter
public class ExpoRetryableException extends RuntimeException {
    private final String errorCode;

    public ExpoRetryableException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}