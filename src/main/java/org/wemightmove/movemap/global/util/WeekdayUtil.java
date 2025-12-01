package org.wemightmove.movemap.global.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 요일 비트마스크 변환 유틸리티
 *
 * 비트마스크 구조 (Python 스크립트와 동일):
 * - 비트 0 (1): 월요일
 * - 비트 1 (2): 화요일
 * - 비트 2 (4): 수요일
 * - 비트 3 (8): 목요일
 * - 비트 4 (16): 금요일
 * - 비트 5 (32): 토요일
 * - 비트 6 (64): 일요일
 *
 * 예시:
 * - 31 (0011111) = 월~금 (평일)
 * - 96 (1100000) = 토, 일 (주말)
 * - 127 (1111111) = 매일 (월~일)
 */
@Slf4j
public class WeekdayUtil {

    // Python 스크립트와 동일한 순서
    private static final String[] WEEKDAY_NAMES = {
            "월", "화", "수", "목", "금", "토", "일"
            // 0    1    2    3    4    5    6
    };

    private static final int MONDAY_BIT = 1;      // 0000001 = 2^0
    private static final int TUESDAY_BIT = 2;     // 0000010 = 2^1
    private static final int WEDNESDAY_BIT = 4;   // 0000100 = 2^2
    private static final int THURSDAY_BIT = 8;    // 0001000 = 2^3
    private static final int FRIDAY_BIT = 16;     // 0010000 = 2^4
    private static final int SATURDAY_BIT = 32;   // 0100000 = 2^5
    private static final int SUNDAY_BIT = 64;     // 1000000 = 2^6
    private static final int ALL_DAYS_MASK = 127; // 1111111

    // 그룹 마스크
    private static final int WEEKDAYS_MASK =
            MONDAY_BIT | TUESDAY_BIT | WEDNESDAY_BIT | THURSDAY_BIT | FRIDAY_BIT; // 31

    private static final int WEEKEND_MASK =
            SATURDAY_BIT | SUNDAY_BIT; // 96

    /**
     * 비트마스크를 요일 문자열 배열로 디코딩
     *
     * @param weekdayBitmask 요일 비트마스크 (0-127)
     * @return 요일 문자열 배열 (예: ["월", "화", "수"])
     */
    public static String[] decodeWeekdays(Integer weekdayBitmask) {
        if (weekdayBitmask == null || weekdayBitmask < 0 || weekdayBitmask > ALL_DAYS_MASK) {
            log.warn("유효하지 않은 요일 비트마스크: {}", weekdayBitmask);
            return new String[0];
        }

        // 매일인 경우 최적화
        if (weekdayBitmask == ALL_DAYS_MASK) {
            return new String[]{"매일"};
        }

        // 요일이 없는 경우
        if (weekdayBitmask == 0) {
            return new String[0];
        }

        List<String> weekdays = new ArrayList<>(7);

        // 각 비트를 체크하여 해당 요일 추가 (월~일 순서)
        for (int i = 0; i < 7; i++) {
            int bitMask = 1 << i;
            if ((weekdayBitmask & bitMask) != 0) {
                weekdays.add(WEEKDAY_NAMES[i]);
            }
        }

        log.debug("요일 디코딩 완료 - 비트마스크: {}, 결과: {}",
                weekdayBitmask, String.join(", ", weekdays));

        return weekdays.toArray(new String[0]);
    }

    /**
     * 비트마스크를 간소화된 요일 문자열로 디코딩
     * 예: ["월", "화", "수", "목", "금"] → "평일"
     *
     * @param weekdayBitmask 요일 비트마스크
     * @return 간소화된 요일 문자열 (예: "평일", "주말", "월,수,금")
     */
    public static String decodeWeekdaysSimplified(Integer weekdayBitmask) {
        if (weekdayBitmask == null || weekdayBitmask < 0 || weekdayBitmask > ALL_DAYS_MASK) {
            return "";
        }

        if (weekdayBitmask == ALL_DAYS_MASK) {
            return "매일";
        }

        if (weekdayBitmask == 0) {
            return "";
        }

        // 평일 전체
        if (weekdayBitmask == WEEKDAYS_MASK) {
            return "평일";
        }

        // 주말 전체
        if (weekdayBitmask == WEEKEND_MASK) {
            return "주말";
        }

        // 평일 + 주말 = 매일
        if ((weekdayBitmask & WEEKDAYS_MASK) == WEEKDAYS_MASK &&
                (weekdayBitmask & WEEKEND_MASK) == WEEKEND_MASK) {
            return "매일";
        }

        // 개별 요일 나열
        String[] weekdays = decodeWeekdays(weekdayBitmask);
        return String.join(",", weekdays);
    }

    /**
     * 요일 문자열 배열을 비트마스크로 인코딩
     *
     * @param weekdays 요일 문자열 배열 (예: ["월", "화", "수"])
     * @return 비트마스크 (0-127)
     */
    public static int encodeWeekdays(String[] weekdays) {
        if (weekdays == null || weekdays.length == 0) {
            return 0;
        }

        int bitmask = 0;

        for (String weekday : weekdays) {
            String normalized = weekday.trim();

            // 정확한 매칭
            for (int i = 0; i < WEEKDAY_NAMES.length; i++) {
                if (WEEKDAY_NAMES[i].equals(normalized)) {
                    bitmask |= (1 << i);
                    break;
                }
            }

            // 특수 키워드 처리
            switch (normalized) {
                case "평일":
                    bitmask |= WEEKDAYS_MASK;
                    break;
                case "주말":
                    bitmask |= WEEKEND_MASK;
                    break;
                case "매일", "전체":
                    return ALL_DAYS_MASK;
            }
        }

        log.debug("요일 인코딩 완료 - 입력: {}, 비트마스크: {}",
                String.join(", ", weekdays), bitmask);

        return bitmask;
    }

    /**
     * 요일 문자열을 비트마스크로 변환 (Python 스크립트와 동일한 로직)
     * "월화수" → 7 (1 + 2 + 4)
     *
     * @param dayString 요일 문자열 (예: "월화수", "월,화,수", "평일")
     * @return 비트마스크 (0-127)
     */
    public static int encodeWeekdayString(String dayString) {
        if (dayString == null || dayString.trim().isEmpty()) {
            return 0;
        }

        String normalized = dayString.trim()
                .replace(",", "")
                .replace(" ", "")
                .replace(".", "");

        // 특수 키워드
        if (normalized.contains("매일") || normalized.contains("전체")) {
            return ALL_DAYS_MASK;
        }
        if (normalized.contains("평일")) {
            return WEEKDAYS_MASK;
        }
        if (normalized.contains("주말")) {
            return WEEKEND_MASK;
        }

        int bitmask = 0;

        // 각 요일 문자를 확인
        for (int i = 0; i < WEEKDAY_NAMES.length; i++) {
            if (normalized.contains(WEEKDAY_NAMES[i])) {
                bitmask |= (1 << i);
            }
        }

        log.debug("요일 문자열 인코딩 완료 - 입력: '{}', 비트마스크: {}",
                dayString, bitmask);

        return bitmask;
    }

    /**
     * 특정 요일이 비트마스크에 포함되어 있는지 확인
     *
     * @param weekdayBitmask 요일 비트마스크
     * @param weekday 요일 문자열 ("월", "화", ...)
     * @return 포함 여부
     */
    public static boolean contains(Integer weekdayBitmask, String weekday) {
        if (weekdayBitmask == null || weekday == null) {
            return false;
        }

        for (int i = 0; i < WEEKDAY_NAMES.length; i++) {
            if (WEEKDAY_NAMES[i].equals(weekday.trim())) {
                int bitMask = 1 << i;
                return (weekdayBitmask & bitMask) != 0;
            }
        }

        return false;
    }

    /**
     * 특정 요일 인덱스가 비트마스크에 포함되어 있는지 확인
     *
     * @param weekdayBitmask 요일 비트마스크
     * @param weekdayIndex 요일 인덱스 (0=월, 1=화, ..., 6=일)
     * @return 포함 여부
     */
    public static boolean containsByIndex(Integer weekdayBitmask, int weekdayIndex) {
        if (weekdayBitmask == null || weekdayIndex < 0 || weekdayIndex > 6) {
            return false;
        }

        int bitMask = 1 << weekdayIndex;
        return (weekdayBitmask & bitMask) != 0;
    }

    /**
     * 평일(월~금) 전체 포함 여부 확인
     */
    public static boolean isAllWeekdays(Integer weekdayBitmask) {
        return weekdayBitmask != null &&
                (weekdayBitmask & WEEKDAYS_MASK) == WEEKDAYS_MASK;
    }

    /**
     * 주말(토~일) 전체 포함 여부 확인
     */
    public static boolean isAllWeekend(Integer weekdayBitmask) {
        return weekdayBitmask != null &&
                (weekdayBitmask & WEEKEND_MASK) == WEEKEND_MASK;
    }

    /**
     * 월요일 포함 여부 확인
     */
    public static boolean containsMonday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & MONDAY_BIT) != 0;
    }

    /**
     * 화요일 포함 여부 확인
     */
    public static boolean containsTuesday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & TUESDAY_BIT) != 0;
    }

    /**
     * 수요일 포함 여부 확인
     */
    public static boolean containsWednesday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & WEDNESDAY_BIT) != 0;
    }

    /**
     * 목요일 포함 여부 확인
     */
    public static boolean containsThursday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & THURSDAY_BIT) != 0;
    }

    /**
     * 금요일 포함 여부 확인
     */
    public static boolean containsFriday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & FRIDAY_BIT) != 0;
    }

    /**
     * 토요일 포함 여부 확인
     */
    public static boolean containsSaturday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & SATURDAY_BIT) != 0;
    }

    /**
     * 일요일 포함 여부 확인
     */
    public static boolean containsSunday(Integer weekdayBitmask) {
        return weekdayBitmask != null && (weekdayBitmask & SUNDAY_BIT) != 0;
    }

    /**
     * 비트마스크를 사람이 읽을 수 있는 문자열로 변환 (디버깅용)
     *
     * @param weekdayBitmask 요일 비트마스크
     * @return 문자열 표현 (예: "평일", "월,수,금", "매일")
     */
    public static String toString(Integer weekdayBitmask) {
        if (weekdayBitmask == null || weekdayBitmask == 0) {
            return "없음";
        }

        return decodeWeekdaysSimplified(weekdayBitmask);
    }
}