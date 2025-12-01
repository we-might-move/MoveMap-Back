package org.wemightmove.movemap.global.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 프로그램 필터링을 위한 비트마스크 유틸리티
 */
@Slf4j
public class ProgramBitmaskUtil {

    // 요일 비트 상수
    private static final int MONDAY = 1;      // 2^0 = 1
    private static final int TUESDAY = 2;     // 2^1 = 2
    private static final int WEDNESDAY = 4;   // 2^2 = 4
    private static final int THURSDAY = 8;    // 2^3 = 8
    private static final int FRIDAY = 16;     // 2^4 = 16
    private static final int SATURDAY = 32;   // 2^5 = 32
    private static final int SUNDAY = 64;     // 2^6 = 64

    // 연령 대상 비트 위치
    private static final int ELEMENTARY_1 = 0;   // 초등 1학년 (8세)
    private static final int ELEMENTARY_6 = 5;   // 초등 6학년 (13세)
    private static final int MIDDLE_1 = 6;       // 중등 1학년 (14세)
    private static final int MIDDLE_3 = 8;       // 중등 3학년 (16세)
    private static final int HIGH_1 = 9;         // 고등 1학년 (17세)
    private static final int HIGH_3 = 11;        // 고등 3학년 (19세)
    private static final int ADULT = 12;         // 성인 (20~59세)
    private static final int SENIOR = 13;        // 시니어 (60세+)

    private static final int ALL_TARGETS = (1 << 14) - 1; // 모든 비트 켜짐 = 16383

    /**
     * 요일 리스트를 비트마스크로 변환
     *
     * @param weekdays 요일 리스트 (1=월, 2=화, ..., 7=일)
     * @return 비트마스크 값
     */
    public static int weekdaysToBitmask(List<Integer> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return 0;
        }

        int bitmask = 0;
        for (Integer weekday : weekdays) {
            if (weekday < 1 || weekday > 7) {
                log.warn("Invalid weekday value: {}. Skipping.", weekday);
                continue;
            }
            bitmask |= (1 << (weekday - 1));
        }
        return bitmask;
    }

    /**
     * 연령 범위를 비트마스크로 변환
     *
     * @param minAge 최소 나이 (nullable)
     * @param maxAge 최대 나이 (nullable)
     * @return 비트마스크 값
     */
    public static int ageToBitmask(Integer minAge, Integer maxAge) {
        // 둘 다 없으면 전체
        if (minAge == null && maxAge == null) {
            return ALL_TARGETS;
        }

        int actualMinAge = minAge != null ? minAge : 8;
        int actualMaxAge = maxAge != null ? maxAge : 100;

        int bitmask = 0;
        for (int age = actualMinAge; age <= actualMaxAge; age++) {
            int bitPosition = ageToBitPosition(age);
            if (bitPosition >= 0) {
                bitmask |= (1 << bitPosition);
            }
        }

        return bitmask;
    }

    /**
     * 나이를 비트 위치로 변환
     */
    private static int ageToBitPosition(int age) {
        if (age >= 8 && age <= 13) {
            return age - 8;  // 초등 1~6학년: 비트 0~5
        } else if (age >= 14 && age <= 16) {
            return age - 14 + 6;  // 중등 1~3학년: 비트 6~8
        } else if (age >= 17 && age <= 19) {
            return age - 17 + 9;  // 고등 1~3학년: 비트 9~11
        } else if (age >= 20 && age <= 59) {
            return 12;  // 성인
        } else if (age >= 60) {
            return 13;  // 시니어
        }
        return -1;
    }

    /**
     * 비트마스크를 요일 리스트로 변환 (디버깅용)
     */
    public static List<Integer> bitmaskToWeekdays(int bitmask) {
        List<Integer> weekdays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if ((bitmask & (1 << i)) != 0) {
                weekdays.add(i + 1);
            }
        }
        return weekdays;
    }

    /**
     * 비트마스크를 연령 범위 문자열로 변환 (디버깅용)
     */
    public static String bitmaskToAgeRangeString(int bitmask) {
        List<String> ranges = new ArrayList<>();

        if ((bitmask & ((1 << 6) - 1)) != 0) {
            ranges.add("초등학생");
        }
        if ((bitmask & (((1 << 9) - 1) ^ ((1 << 6) - 1))) != 0) {
            ranges.add("중학생");
        }
        if ((bitmask & (((1 << 12) - 1) ^ ((1 << 9) - 1))) != 0) {
            ranges.add("고등학생");
        }
        if ((bitmask & (1 << ADULT)) != 0) {
            ranges.add("성인");
        }
        if ((bitmask & (1 << SENIOR)) != 0) {
            ranges.add("시니어");
        }

        return String.join(", ", ranges);
    }
}
