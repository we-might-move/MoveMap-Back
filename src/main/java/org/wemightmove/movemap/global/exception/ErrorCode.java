package org.wemightmove.movemap.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /*
     * 공통적으로 발생하는 오류
     * (1000 ~ 1999)
     */
    BAD_REQUEST(400, 1000, "요청의 형식이나 내용이 잘못되었습니다."),
    MISSING_PARAMETER(400, 1001, "필수 파라미터가 누락되었습니다."),
    INVALID_ENUM_VALUE(400, 1002, "잘못된 ENUM 값입니다."),
    INVALID_FILE_FORMAT(400, 1500, "잘못된 파일 형식입니다."),
    UNSUPPORTED_FILE_FORMAT(400, 1501, "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAIL(500, 1502, "파일 업로드에 실패했습니다."),
    FILE_COMPARISON_FAIL(500, 1503, "파일 비교에 실패했습니다."),
    ALREADY_PROCESSED(409, 1600, "이미 처리된 요청입니다."),
    EXTERNAL_API_ERROR(500, 1900, "외부 API 호출 중 오류가 발생했습니다."),

    /*
     * 인증/인가 관련 오류
     * (2000 ~ 2999)
     */
    UNAUTHORIZED(401, 2000, "인증 정보가 누락되거나 잘못되었습니다."),
    ACCESS_DENIED(403, 2001, "접근 권한이 없습니다."),
    INVALID_JWT_SIGNATURE(401, 2003, "잘못된 JWT 서명입니다."),
    INVALID_TOKEN(401, 2100, "잘못된 토큰입니다."),
    NO_COOKIE(404, 2101, "쿠키가 존재하지 않습니다."),
    EXPIRED_TOKEN(401, 2300, "만료된 토큰입니다."),

    /*
     * 리소스 관련 오류 (Member)
     * (3000 ~ 3999)
     */

    MEMBER_NOT_FOUND(404, 3000, "사용자를 찾을 수 없습니다."),
    MEMBER_DELETED(404, 3001, "탈퇴한 사용자입니다."),

    RESOURCE_NOT_FOUND(404, 5000, "리소스를 찾을 수 없습니다."),

    /*
     * FCM 관련 오류 (Notification)
     * (4000 ~ 4999)
     */
    INVALID_FCM_TOKEN(401, 4000, "유효하지 않은 FCM 토큰입니다. 재등록해주세요."),
    DEVICE_NOT_FOUND(404, 4001, "기기를 찾을 수 없습니다. 등록 후 사용해주세요.");

    private final int status;
    private final int code;
    private final String message;

}
