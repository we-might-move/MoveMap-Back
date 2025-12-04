package org.wemightmove.movemap.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramBitmaskUtilTest {

    @Test
    @DisplayName("요일 비트마스크 변환 - 월수금")
    void weekdaysToBitmask_MonWedFri() {
        // given
        List<Integer> weekdays = List.of(1, 3, 5); // 월, 수, 금

        // when
        int bitmask = ProgramBitmaskUtil.weekdaysToBitmask(weekdays);

        // then
        // 1(2^0) | 4(2^2) | 16(2^4) = 21 (0b10101)
        assertThat(bitmask).isEqualTo(21);
    }

    @Test
    @DisplayName("요일 비트마스크 변환 - 주말")
    void weekdaysToBitmask_Weekend() {
        // given
        List<Integer> weekdays = List.of(6, 7); // 토, 일

        // when
        int bitmask = ProgramBitmaskUtil.weekdaysToBitmask(weekdays);

        // then
        // 32(2^5) | 64(2^6) = 96 (0b1100000)
        assertThat(bitmask).isEqualTo(96);
    }

    @Test
    @DisplayName("연령 비트마스크 변환 - 초등학생 전체")
    void ageToBitmask_Elementary() {
        // given
        Integer minAge = 8;
        Integer maxAge = 13;

        // when
        int bitmask = ProgramBitmaskUtil.ageToBitmask(minAge, maxAge);

        // then
        // 초등 1~6학년 (비트 0~5)
        // 2^0 | 2^1 | 2^2 | 2^3 | 2^4 | 2^5 = 63 (0b111111)
        assertThat(bitmask).isEqualTo(63);
    }

    @Test
    @DisplayName("연령 비트마스크 변환 - 청소년 (중고등)")
    void ageToBitmask_Teenager() {
        // given
        Integer minAge = 14;
        Integer maxAge = 19;

        // when
        int bitmask = ProgramBitmaskUtil.ageToBitmask(minAge, maxAge);

        // then
        // 중등 1~3학년(비트 6~8) + 고등 1~3학년(비트 9~11)
        // 2^6 | 2^7 | 2^8 | 2^9 | 2^10 | 2^11 = 4032 (0b111111000000)
        assertThat(bitmask).isEqualTo(4032);
    }

    @Test
    @DisplayName("연령 비트마스크 변환 - 성인")
    void ageToBitmask_Adult() {
        // given
        Integer minAge = 20;
        Integer maxAge = 59;

        // when
        int bitmask = ProgramBitmaskUtil.ageToBitmask(minAge, maxAge);

        // then
        // 성인 (비트 12)
        // 2^12 = 4096
        assertThat(bitmask).isEqualTo(4096);
    }

    @Test
    @DisplayName("연령 비트마스크 변환 - 전체 (null, null)")
    void ageToBitmask_All() {
        // given
        Integer minAge = null;
        Integer maxAge = null;

        // when
        int bitmask = ProgramBitmaskUtil.ageToBitmask(minAge, maxAge);

        // then
        // 모든 비트 (0~13)
        // 2^14 - 1 = 16383
        assertThat(bitmask).isEqualTo(16383);
    }

    @Test
    @DisplayName("비트마스크 → 요일 변환 (디코딩)")
    void bitmaskToWeekdays() {
        // given
        int bitmask = 21; // 월수금

        // when
        List<Integer> weekdays = ProgramBitmaskUtil.bitmaskToWeekdays(bitmask);

        // then
        assertThat(weekdays).containsExactly(1, 3, 5);
    }

    @Test
    @DisplayName("비트마스크 → 연령 범위 문자열 변환")
    void bitmaskToAgeRangeString() {
        // given
        int bitmask = 63; // 초등학생 전체

        // when
        String ageRange = ProgramBitmaskUtil.bitmaskToAgeRangeString(bitmask);

        // then
        assertThat(ageRange).isEqualTo("초등학생");
    }
}