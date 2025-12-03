package org.wemightmove.movemap.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WeekdayUtilTest {

    @Test
    @DisplayName("평일 전체 비트마스크 디코딩")
    void decodeWeekdays() {
        // given
        int bitmask = 31; // 0011111 = 월~금

        // when
        String[] result = WeekdayUtil.decodeWeekdays(bitmask);

        // then
        assertThat(result).containsExactly("월", "화", "수", "목", "금");
    }

    @Test
    @DisplayName("주말 비트마스크 디코딩")
    void decodeWeekend() {
        // given
        int bitmask = 96; // 1100000 = 토, 일

        // when
        String[] result = WeekdayUtil.decodeWeekdays(bitmask);

        // then
        assertThat(result).containsExactly("토", "일");
    }

    @Test
    @DisplayName("매일 비트마스크 디코딩")
    void decodeEveryDay() {
        // given
        int bitmask = 127; // 1111111 = 매일

        // when
        String[] result = WeekdayUtil.decodeWeekdays(bitmask);

        // then
        assertThat(result).containsExactly("매일");
    }

    @Test
    @DisplayName("평일 비트마스크 간소화 디코딩")
    void decodeWeekdaysSimplified() {
        // given
        int bitmask = 31; // 평일

        // when
        String result = WeekdayUtil.decodeWeekdaysSimplified(bitmask);

        // then
        assertThat(result).isEqualTo("평일");
    }

    @Test
    @DisplayName("주말 비트마스크 간소화 디코딩")
    void decodeWeekendSimplified() {
        // given
        int bitmask = 96; // 주말

        // when
        String result = WeekdayUtil.decodeWeekdaysSimplified(bitmask);

        // then
        assertThat(result).isEqualTo("주말");
    }

    @Test
    @DisplayName("개별 요일 비트마스크 간소화 디코딩")
    void decodeIndividualDaysSimplified() {
        // given
        int bitmask = 21; // 0010101 = 월, 수, 금

        // when
        String result = WeekdayUtil.decodeWeekdaysSimplified(bitmask);

        // then
        assertThat(result).isEqualTo("월,수,금");
    }

    @Test
    @DisplayName("요일 문자열 배열을 비트마스크로 인코딩")
    void encodeWeekdays() {
        // given
        String[] weekdays = {"월", "수", "금"};

        // when
        int result = WeekdayUtil.encodeWeekdays(weekdays);

        // then
        // 1 + 4 + 16 = 21
        assertThat(result).isEqualTo(21);
    }

    @Test
    @DisplayName("평일 키워드를 비트마스크로 인코딩")
    void encodeWeekdaysKeyword() {
        // given
        String[] weekdays = {"평일"};

        // when
        int result = WeekdayUtil.encodeWeekdays(weekdays);

        // then
        assertThat(result).isEqualTo(31); // 월~금
    }

    @Test
    @DisplayName("요일 문자열을 비트마스크로 변환 - 공백 없음")
    void encodeWeekdayString() {
        // given
        String dayString = "월화수";

        // when
        int result = WeekdayUtil.encodeWeekdayString(dayString);

        // then
        // 1 + 2 + 4 = 7
        assertThat(result).isEqualTo(7);
    }

    @Test
    @DisplayName("요일 문자열을 비트마스크로 변환 - 쉼표 구분")
    void encodeWeekdayStringWithComma() {
        // given
        String dayString = "월,수,금";

        // when
        int result = WeekdayUtil.encodeWeekdayString(dayString);

        // then
        // 1 + 4 + 16 = 21
        assertThat(result).isEqualTo(21);
    }

    @Test
    @DisplayName("요일 문자열을 비트마스크로 변환 - 평일 키워드")
    void encodeWeekdayStringWeekdays() {
        // given
        String dayString = "평일";

        // when
        int result = WeekdayUtil.encodeWeekdayString(dayString);

        // then
        assertThat(result).isEqualTo(31);
    }

    @Test
    @DisplayName("특정 요일 포함 여부 확인")
    void contains() {
        // given
        int bitmask = 31; // 평일

        // when & then
        assertThat(WeekdayUtil.contains(bitmask, "월")).isTrue();
        assertThat(WeekdayUtil.contains(bitmask, "금")).isTrue();
        assertThat(WeekdayUtil.contains(bitmask, "토")).isFalse();
        assertThat(WeekdayUtil.contains(bitmask, "일")).isFalse();
    }

    @Test
    @DisplayName("평일 전체 포함 여부 확인")
    void isAllWeekdays() {
        // given
        int weekdays = 31;       // 월~금
        int partial = 7;         // 월~수
        int withWeekend = 127;   // 매일

        // when & then
        assertThat(WeekdayUtil.isAllWeekdays(weekdays)).isTrue();
        assertThat(WeekdayUtil.isAllWeekdays(partial)).isFalse();
        assertThat(WeekdayUtil.isAllWeekdays(withWeekend)).isTrue();
    }

    @Test
    @DisplayName("주말 전체 포함 여부 확인")
    void isAllWeekend() {
        // given
        int weekend = 96;  // 토, 일
        int saturday = 32; // 토요일만

        // when & then
        assertThat(WeekdayUtil.isAllWeekend(weekend)).isTrue();
        assertThat(WeekdayUtil.isAllWeekend(saturday)).isFalse();
    }

    @Test
    @DisplayName("개별 요일 포함 여부 확인")
    void containsIndividualDays() {
        // given
        int bitmask = 21; // 월, 수, 금

        // when & then
        assertThat(WeekdayUtil.containsMonday(bitmask)).isTrue();
        assertThat(WeekdayUtil.containsTuesday(bitmask)).isFalse();
        assertThat(WeekdayUtil.containsWednesday(bitmask)).isTrue();
        assertThat(WeekdayUtil.containsThursday(bitmask)).isFalse();
        assertThat(WeekdayUtil.containsFriday(bitmask)).isTrue();
        assertThat(WeekdayUtil.containsSaturday(bitmask)).isFalse();
        assertThat(WeekdayUtil.containsSunday(bitmask)).isFalse();
    }

    @Test
    @DisplayName("비트마스크를 문자열로 변환")
    void toStringTest() {
        // given & when & then
        assertThat(WeekdayUtil.toString(31)).isEqualTo("평일");
        assertThat(WeekdayUtil.toString(96)).isEqualTo("주말");
        assertThat(WeekdayUtil.toString(127)).isEqualTo("매일");
        assertThat(WeekdayUtil.toString(21)).isEqualTo("월,수,금");
        assertThat(WeekdayUtil.toString(0)).isEqualTo("없음");
    }

    @Test
    @DisplayName("Python 스크립트와 동일한 비트마스크 생성 확인")
    void pythonCompatibility() {
        // Python: WEEKDAY_BITS['월'] = 1
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"월"}))
                .isEqualTo(1);

        // Python: WEEKDAY_BITS['화'] = 2
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"화"}))
                .isEqualTo(2);

        // Python: WEEKDAY_BITS['수'] = 4
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"수"}))
                .isEqualTo(4);

        // Python: WEEKDAY_BITS['목'] = 8
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"목"}))
                .isEqualTo(8);

        // Python: WEEKDAY_BITS['금'] = 16
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"금"}))
                .isEqualTo(16);

        // Python: WEEKDAY_BITS['토'] = 32
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"토"}))
                .isEqualTo(32);

        // Python: WEEKDAY_BITS['일'] = 64
        assertThat(WeekdayUtil.encodeWeekdays(new String[]{"일"}))
                .isEqualTo(64);

        // 평일: 1 + 2 + 4 + 8 + 16 = 31
        assertThat(WeekdayUtil.encodeWeekdayString("월화수목금"))
                .isEqualTo(31);

        // 주말: 32 + 64 = 96
        assertThat(WeekdayUtil.encodeWeekdayString("토일"))
                .isEqualTo(96);
    }
}