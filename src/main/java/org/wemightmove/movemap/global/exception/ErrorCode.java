package org.wemightmove.movemap.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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

    /*
     * 인증/인가 관련 오류
     * (2000 ~ 2999)
     */
    UNAUTHORIZED(401, 2000, "인증 정보가 누락되거나 잘못되었습니다."),
    ACCESS_DENIED(403, 2001, "접근 권한이 없습니다."),
    INVALID_JWT_SIGNATURE(401, 2003, "잘못된 JWT 서명입니다."),
    INVALID_TOKEN(401, 2100, "잘못된 토큰입니다."),
    NO_COOKIE(404, 2101, "쿠키가 존재하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(401, 2300, "만료된 엑세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, 2301, "만료된 리프레쉬 토큰입니다."),

    /*
     * Redis 관련 오류
     * 5001
     */
    FAIL_SERIALIZATION(500, 5001, "직렬화/역직렬화에 실패했습니다."),
    INVITE_EXPIRED(401, 5002, "만료된 초대입니다."),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 5003, "초대 정보를 찾을 수 없습니다."),

    /*
     * Member 관련 오류
     */
    MEMBER_NOT_FOUND(404, 4001, "멤버가 존재하지 않습니다."),
    INVALID_INVITE_CODE(401, 4002, "잘못된 초대 코드입니다."),
    ALREADY_CONNECTED(400, 4003, "이미 연결된 부모-자식 관계입니다"),
    ALREADY_SEND_INVITE(400, 4004, "이미 초대를 보냈습니다"),
    INVALID_INVITE_MEMBER(400, 4005, "본인에게는 초대를 보낼 수 없습니다")
    ;

    private final int status;
    private final int code;
    private final String message;

}
