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
    ALREADY_PROCESSED(409, 1600, "이미 처리된 요청입니다."),
    EXTERNAL_API_ERROR(500, 1900, "외부 API 호출 중 오류가 발생했습니다."),
    SERVER_ERROR(500, 1999, "요청 처리 중 오류가 발생했습니다."),


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
    EXPIRED_ACCESS_TOKEN(401, 2301, "만료된 엑세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, 2302, "만료된 리프레쉬 토큰입니다."),
    RESOURCE_NOT_FOUND(404, 4000, "리소스를 찾을 수 없습니다."),

    /*
     * 리소스 관련 오류 (Member, Notification, Facility)
     * (3000 ~ 3999)
     */
    MEMBER_NOT_FOUND(404, 3000, "사용자를 찾을 수 없습니다."),
    MEMBER_DELETED(404, 3001, "탈퇴한 사용자입니다."),
    INVALID_FCM_TOKEN(401, 3002, "유효하지 않은 FCM 토큰입니다. 재등록해주세요."),
    DEVICE_NOT_FOUND(404, 3003, "기기를 찾을 수 없습니다. 등록 후 사용해주세요."),
    INVALID_INVITE_CODE(401, 3004, "잘못된 초대 코드입니다."),
    ALREADY_CONNECTED(400, 3005, "이미 연결된 부모-자식 관계입니다"),
    ALREADY_SEND_INVITE(400, 3006, "이미 초대를 보냈습니다"),
    INVALID_INVITE_MEMBER(400, 3007, "본인에게는 초대를 보낼 수 없습니다"),

    DUPLICATE_NICKNAME(400, 3008, "유효하지 않은 닉네임입니다"),
    INVALID_REGION_CITY(400, 3009, "유효하지 않은 지역(시/도)입니다"),
    INVALID_REGION_DISTRICT(400, 3010, "유효하지 않은 지역(시/군/구)입니다"),
    INVALID_REGION_UPDATE(400, 3011, "지역 정보를 변경하려면 시/도와 구/군을 모두 입력해야 합니다"),
    INVALID_REGION_FAIR(400, 3012, "부모 지역(시/도)과 포함 지역(시/군/구)의 짝이 알맞지 않습니다."),
    INVALID_SEX(400, 3013, "유효하지 않은 성별입니다"),
    INVALID_AGE(400, 3014, "유효하지 않은 나이입니다"),
    INVALID_HEIGHT(400, 3015, "유효하지 않은 키 값입니다"),
    INVALID_WEIGHT(400, 3016, "유효하지 않은 몸무게 값입니다"),

    FACILITY_NOT_FOUND(404, 3017, "시설을 찾을 수 없습니다."),
    ALREADY_ADDED_BOOKMARK(400, 3018, "이미 등록한 북마크입니다."),
    ALREADY_DELETED_BOOKMARK(400, 3018, "이미 삭제한 북마크입니다"),
    ALREADY_ADDED_FACILITY_REVIEW(400, 3018, "이미 리뷰를 작성한 시설입니다."),

    CHILD_NOT_FOUND(404, 3019, "등록된 자식을 찾을 수 없습니다."),

    /*
     * Redis 관련 오류
     * (4000 ~ 4999)
     */
    FAIL_SERIALIZATION(500, 4001, "직렬화/역직렬화에 실패했습니다."),
    INVITE_EXPIRED(401, 4002, "만료된 초대입니다."),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4003, "초대 정보를 찾을 수 없습니다."),

    /*
     * 위도, 경도 오류
     * (5000 ~ 5999)
     */
    WRONG_LATITUDE(400, 5001, "위도는 -90에서 90 사이여야 합니다."),
    WRONG_LONGITUDE(400, 5002, "경도는 -180에서 180 사이여야 합니다.");

    private final int status;
    private final int code;
    private final String message;

}
