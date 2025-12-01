package org.wemightmove.movemap.global.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 연령대 비트마스크 변환 유틸리티 (14비트)
 *
 * 비트마스크 구조:
 * - 비트 0 (1): 초등학교 1학년
 * - 비트 1 (2): 초등학교 2학년
 * - 비트 2 (4): 초등학교 3학년
 * - 비트 3 (8): 초등학교 4학년
 * - 비트 4 (16): 초등학교 5학년
 * - 비트 5 (32): 초등학교 6학년
 * - 비트 6 (64): 중학교 1학년
 * - 비트 7 (128): 중학교 2학년
 * - 비트 8 (256): 중학교 3학년
 * - 비트 9 (512): 고등학교 1학년
 * - 비트 10 (1024): 고등학교 2학년
 * - 비트 11 (2048): 고등학교 3학년
 * - 비트 12 (4096): 성인 (19-64세)
 * - 비트 13 (8192): 시니어 (65세 이상)
 *
 * 예시:
 * - 7 (0000000000111) = 초등 1-3학년
 * - 16383 (11111111111111) = 전연령
 * - 4096 (1000000000000) = 성인
 */
@Slf4j
public class AgeGroupUtil {

    // 초등학교 (비트 0-5)
    private static final int ELEMENTARY_1 = 1;        // 1 << 0
    private static final int ELEMENTARY_2 = 2;        // 1 << 1
    private static final int ELEMENTARY_3 = 4;        // 1 << 2
    private static final int ELEMENTARY_4 = 8;        // 1 << 3
    private static final int ELEMENTARY_5 = 16;       // 1 << 4
    private static final int ELEMENTARY_6 = 32;       // 1 << 5

    // 중학교 (비트 6-8)
    private static final int MIDDLE_1 = 64;           // 1 << 6
    private static final int MIDDLE_2 = 128;          // 1 << 7
    private static final int MIDDLE_3 = 256;          // 1 << 8

    // 고등학교 (비트 9-11)
    private static final int HIGH_1 = 512;            // 1 << 9
    private static final int HIGH_2 = 1024;           // 1 << 10
    private static final int HIGH_3 = 2048;           // 1 << 11

    // 성인/시니어 (비트 12-13)
    private static final int ADULT = 4096;            // 1 << 12
    private static final int SENIOR = 8192;           // 1 << 13

    // 전체 마스크
    private static final int ALL_AGES_MASK = 16383;   // 0x3FFF = 11111111111111

    // 그룹 마스크
    private static final int ELEMENTARY_ALL =
            ELEMENTARY_1 | ELEMENTARY_2 | ELEMENTARY_3 | ELEMENTARY_4 | ELEMENTARY_5 | ELEMENTARY_6; // 63

    private static final int ELEMENTARY_LOW =
            ELEMENTARY_1 | ELEMENTARY_2 | ELEMENTARY_3; // 7

    private static final int ELEMENTARY_HIGH =
            ELEMENTARY_4 | ELEMENTARY_5 | ELEMENTARY_6; // 56

    private static final int MIDDLE_ALL =
            MIDDLE_1 | MIDDLE_2 | MIDDLE_3; // 448

    private static final int HIGH_ALL =
            HIGH_1 | HIGH_2 | HIGH_3; // 3584

    private static final int TEENAGER_ALL =
            MIDDLE_ALL | HIGH_ALL; // 4032

    // 연령대 이름 배열 (14개)
    private static final String[] AGE_GROUP_NAMES = {
            "초등학교 1학년",  // 비트 0
            "초등학교 2학년",  // 비트 1
            "초등학교 3학년",  // 비트 2
            "초등학교 4학년",  // 비트 3
            "초등학교 5학년",  // 비트 4
            "초등학교 6학년",  // 비트 5
            "중학교 1학년",    // 비트 6
            "중학교 2학년",    // 비트 7
            "중학교 3학년",    // 비트 8
            "고등학교 1학년",  // 비트 9
            "고등학교 2학년",  // 비트 10
            "고등학교 3학년",  // 비트 11
            "성인",            // 비트 12
            "시니어"           // 비트 13
    };

    /**
     * 비트마스크를 연령대 문자열 배열로 디코딩
     *
     * @param ageGroupBitmask 연령대 비트마스크 (0-16383)
     * @return 연령대 문자열 배열
     */
    public static String[] decodeAgeGroups(Integer ageGroupBitmask) {
        if (ageGroupBitmask == null || ageGroupBitmask < 0 || ageGroupBitmask > ALL_AGES_MASK) {
            log.warn("유효하지 않은 연령대 비트마스크: {}", ageGroupBitmask);
            return new String[0];
        }

        // 전연령인 경우
        if (ageGroupBitmask == ALL_AGES_MASK) {
            return new String[]{"전연령"};
        }

        // 연령대가 없는 경우
        if (ageGroupBitmask == 0) {
            return new String[0];
        }

        List<String> ageGroups = new ArrayList<>(14);

        // 각 비트를 체크하여 해당 연령대 추가
        for (int i = 0; i < 14; i++) {
            int bitMask = 1 << i;
            if ((ageGroupBitmask & bitMask) != 0) {
                ageGroups.add(AGE_GROUP_NAMES[i]);
            }
        }

        log.debug("연령대 디코딩 완료 - 비트마스크: {}, 결과: {}",
                ageGroupBitmask, String.join(", ", ageGroups));

        return ageGroups.toArray(new String[0]);
    }

    /**
     * 비트마스크를 간소화된 연령대 문자열 배열로 디코딩
     * 예: ["초등학교 1학년", "초등학교 2학년", ...] → ["초등학생"]
     *
     * @param ageGroupBitmask 연령대 비트마스크
     * @return 간소화된 연령대 문자열 배열
     */
    public static String[] decodeAgeGroupsSimplified(Integer ageGroupBitmask) {
        if (ageGroupBitmask == null || ageGroupBitmask < 0 || ageGroupBitmask > ALL_AGES_MASK) {
            return new String[0];
        }

        if (ageGroupBitmask == ALL_AGES_MASK) {
            return new String[]{"전연령"};
        }

        if (ageGroupBitmask == 0) {
            return new String[0];
        }

        List<String> groups = new ArrayList<>();

        // 초등학생 전체 체크
        if ((ageGroupBitmask & ELEMENTARY_ALL) == ELEMENTARY_ALL) {
            groups.add("초등학생");
        } else {
            // 초등 저학년
            if ((ageGroupBitmask & ELEMENTARY_LOW) == ELEMENTARY_LOW) {
                groups.add("초등 저학년");
            } else {
                if ((ageGroupBitmask & ELEMENTARY_1) != 0) groups.add("초등 1학년");
                if ((ageGroupBitmask & ELEMENTARY_2) != 0) groups.add("초등 2학년");
                if ((ageGroupBitmask & ELEMENTARY_3) != 0) groups.add("초등 3학년");
            }

            // 초등 고학년
            if ((ageGroupBitmask & ELEMENTARY_HIGH) == ELEMENTARY_HIGH) {
                groups.add("초등 고학년");
            } else {
                if ((ageGroupBitmask & ELEMENTARY_4) != 0) groups.add("초등 4학년");
                if ((ageGroupBitmask & ELEMENTARY_5) != 0) groups.add("초등 5학년");
                if ((ageGroupBitmask & ELEMENTARY_6) != 0) groups.add("초등 6학년");
            }
        }

        // 중학생 체크
        if ((ageGroupBitmask & MIDDLE_ALL) == MIDDLE_ALL) {
            groups.add("중학생");
        } else {
            if ((ageGroupBitmask & MIDDLE_1) != 0) groups.add("중학교 1학년");
            if ((ageGroupBitmask & MIDDLE_2) != 0) groups.add("중학교 2학년");
            if ((ageGroupBitmask & MIDDLE_3) != 0) groups.add("중학교 3학년");
        }

        // 고등학생 체크
        if ((ageGroupBitmask & HIGH_ALL) == HIGH_ALL) {
            groups.add("고등학생");
        } else {
            if ((ageGroupBitmask & HIGH_1) != 0) groups.add("고등학교 1학년");
            if ((ageGroupBitmask & HIGH_2) != 0) groups.add("고등학교 2학년");
            if ((ageGroupBitmask & HIGH_3) != 0) groups.add("고등학교 3학년");
        }

        // 성인/시니어
        if ((ageGroupBitmask & ADULT) != 0) groups.add("성인");
        if ((ageGroupBitmask & SENIOR) != 0) groups.add("시니어");

        return groups.toArray(new String[0]);
    }

    /**
     * 연령대 문자열 배열을 비트마스크로 인코딩
     *
     * @param ageGroups 연령대 문자열 배열
     * @return 비트마스크 (0-16383)
     */
    public static int encodeAgeGroups(String[] ageGroups) {
        if (ageGroups == null || ageGroups.length == 0) {
            return 0;
        }

        int bitmask = 0;

        for (String ageGroup : ageGroups) {
            String normalized = ageGroup.trim();

            // 정확한 매칭
            for (int i = 0; i < AGE_GROUP_NAMES.length; i++) {
                if (AGE_GROUP_NAMES[i].equals(normalized)) {
                    bitmask |= (1 << i);
                    break;
                }
            }

            // 그룹 매칭
            switch (normalized) {
                case "초등학생", "초등부", "어린이":
                    bitmask |= ELEMENTARY_ALL;
                    break;
                case "초등 저학년":
                    bitmask |= ELEMENTARY_LOW;
                    break;
                case "초등 고학년":
                    bitmask |= ELEMENTARY_HIGH;
                    break;
                case "중학생":
                    bitmask |= MIDDLE_ALL;
                    break;
                case "고등학생":
                    bitmask |= HIGH_ALL;
                    break;
                case "청소년":
                    bitmask |= TEENAGER_ALL;
                    break;
                case "성인":
                    bitmask |= ADULT;
                    break;
                case "시니어", "노인", "어르신":
                    bitmask |= SENIOR;
                    break;
                case "전체", "전연령", "누구나":
                    return ALL_AGES_MASK;
            }
        }

        log.debug("연령대 인코딩 완료 - 입력: {}, 비트마스크: {}",
                String.join(", ", ageGroups), bitmask);

        return bitmask;
    }

    /**
     * 특정 나이(만 나이)가 비트마스크에 포함되어 있는지 확인
     *
     * @param ageGroupBitmask 연령대 비트마스크
     * @param age 만 나이
     * @return 포함 여부
     */
    public static boolean containsAge(Integer ageGroupBitmask, int age) {
        if (ageGroupBitmask == null || age < 0) {
            return false;
        }

        // 학년 기준으로 비트 인덱스 계산
        int bitIndex = getAgeGroupIndex(age);
        if (bitIndex == -1) {
            return false;
        }

        int bitMask = 1 << bitIndex;
        return (ageGroupBitmask & bitMask) != 0;
    }

    /**
     * 나이를 연령대 인덱스로 변환 (학년 기준)
     *
     * @param age 만 나이
     * @return 연령대 인덱스 (0-13), 유효하지 않은 경우 -1
     */
    private static int getAgeGroupIndex(int age) {
        // 초등학교 (만 7-12세 → 학년별)
        if (age == 7) return 0;   // 초등 1학년
        if (age == 8) return 1;   // 초등 2학년
        if (age == 9) return 2;   // 초등 3학년
        if (age == 10) return 3;  // 초등 4학년
        if (age == 11) return 4;  // 초등 5학년
        if (age == 12) return 5;  // 초등 6학년

        // 중학교 (만 13-15세)
        if (age == 13) return 6;  // 중학교 1학년
        if (age == 14) return 7;  // 중학교 2학년
        if (age == 15) return 8;  // 중학교 3학년

        // 고등학교 (만 16-18세)
        if (age == 16) return 9;  // 고등학교 1학년
        if (age == 17) return 10; // 고등학교 2학년
        if (age == 18) return 11; // 고등학교 3학년

        // 성인 (만 19-64세)
        if (age >= 19 && age <= 64) return 12;

        // 시니어 (만 65세 이상)
        if (age >= 65) return 13;

        // 초등학교 미만 (만 0-6세) - 프로그램 대상 없음
        return -1;
    }

    /**
     * 초등학생 전체 포함 여부 확인
     */
    public static boolean isAllElementary(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & ELEMENTARY_ALL) == ELEMENTARY_ALL;
    }

    /**
     * 중학생 전체 포함 여부 확인
     */
    public static boolean isAllMiddleSchool(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & MIDDLE_ALL) == MIDDLE_ALL;
    }

    /**
     * 고등학생 전체 포함 여부 확인
     */
    public static boolean isAllHighSchool(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & HIGH_ALL) == HIGH_ALL;
    }

    /**
     * 청소년(중/고등학생) 전체 포함 여부 확인
     */
    public static boolean isAllTeenager(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & TEENAGER_ALL) == TEENAGER_ALL;
    }

    /**
     * 성인 포함 여부 확인
     */
    public static boolean containsAdult(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & ADULT) != 0;
    }

    /**
     * 시니어 포함 여부 확인
     */
    public static boolean containsSenior(Integer ageGroupBitmask) {
        return ageGroupBitmask != null &&
                (ageGroupBitmask & SENIOR) != 0;
    }

    /**
     * 비트마스크를 사람이 읽을 수 있는 문자열로 변환 (디버깅용)
     *
     * @param ageGroupBitmask 연령대 비트마스크
     * @return 문자열 표현 (예: "초등학생, 중학생, 성인")
     */
    public static String toString(Integer ageGroupBitmask) {
        if (ageGroupBitmask == null || ageGroupBitmask == 0) {
            return "없음";
        }

        if (ageGroupBitmask == ALL_AGES_MASK) {
            return "전연령";
        }

        String[] groups = decodeAgeGroupsSimplified(ageGroupBitmask);
        return String.join(", ", groups);
    }
}