package org.wemightmove.movemap.global.exception;

import lombok.Getter;

/**
 * FCM 재시도 전용 예외 처리
 *
 * 일시적인 네트워크 오류, FMC 서버 오류 등에만 사용
 */
@Getter
public class FcmRetryableException extends RuntimeException {

    private final String errorCode;

    public FcmRetryableException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public FcmRetryableException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
