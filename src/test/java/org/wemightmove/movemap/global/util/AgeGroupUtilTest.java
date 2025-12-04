package org.wemightmove.movemap.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AgeGroupUtilTest {

    @Test
    @DisplayName("초등학생 전체 비트마스크 디코딩")
    void decodeElementaryAll() {
        // given
        int bitmask = 63; // 0000000000111111 = 초등 1-6학년

        // when
        String[] result = AgeGroupUtil.decodeAgeGroups(bitmask);

        // then
        assertThat(result).containsExactly(
                "초등학교 1학년", "초등학교 2학년", "초등학교 3학년",
                "초등학교 4학년", "초등학교 5학년", "초등학교 6학년"
        );
    }

    @Test
    @DisplayName("초등학생 전체 비트마스크 간소화 디코딩")
    void decodeElementaryAllSimplified() {
        // given
        int bitmask = 63;

        // when
        String[] result = AgeGroupUtil.decodeAgeGroupsSimplified(bitmask);

        // then
        assertThat(result).containsExactly("초등학생");
    }

    @Test
    @DisplayName("청소년 전체 비트마스크 디코딩")
    void decodeTeenagerAll() {
        // given
        int bitmask = 4032; // 중학생(448) + 고등학생(3584)

        // when
        String[] result = AgeGroupUtil.decodeAgeGroupsSimplified(bitmask);

        // then
        assertThat(result).containsExactly("중학생", "고등학생");
    }

    @Test
    @DisplayName("전연령 비트마스크 디코딩")
    void decodeAllAges() {
        // given
        int bitmask = 16383; // 11111111111111

        // when
        String[] result = AgeGroupUtil.decodeAgeGroups(bitmask);

        // then
        assertThat(result).containsExactly("전연령");
    }

    @Test
    @DisplayName("연령대 문자열 배열을 비트마스크로 인코딩")
    void encodeAgeGroups() {
        // given
        String[] ageGroups = {"초등학교 1학년", "초등학교 2학년", "중학생"};

        // when
        int result = AgeGroupUtil.encodeAgeGroups(ageGroups);

        // then
        // 1 + 2 + (64 + 128 + 256) = 3 + 448 = 451
        assertThat(result).isEqualTo(451);
    }

    @Test
    @DisplayName("초등학생 그룹 인코딩")
    void encodeElementaryGroup() {
        // given
        String[] ageGroups = {"초등학생"};

        // when
        int result = AgeGroupUtil.encodeAgeGroups(ageGroups);

        // then
        assertThat(result).isEqualTo(63); // 초등 1-6학년 전체
    }

    @Test
    @DisplayName("특정 나이가 비트마스크에 포함되는지 확인")
    void containsAge() {
        // given
        int bitmask = 63; // 초등학생 전체

        // when & then
        assertThat(AgeGroupUtil.containsAge(bitmask, 7)).isTrue();   // 초등 1학년
        assertThat(AgeGroupUtil.containsAge(bitmask, 12)).isTrue();  // 초등 6학년
        assertThat(AgeGroupUtil.containsAge(bitmask, 13)).isFalse(); // 중학생
        assertThat(AgeGroupUtil.containsAge(bitmask, 19)).isFalse(); // 성인
    }

    @Test
    @DisplayName("초등학생 전체 포함 여부 확인")
    void isAllElementary() {
        // given
        int elementary = 63;        // 초등 전체
        int partial = 7;            // 초등 저학년만
        int withMiddle = 63 + 64;   // 초등 전체 + 중1

        // when & then
        assertThat(AgeGroupUtil.isAllElementary(elementary)).isTrue();
        assertThat(AgeGroupUtil.isAllElementary(partial)).isFalse();
        assertThat(AgeGroupUtil.isAllElementary(withMiddle)).isTrue();
    }

    @Test
    @DisplayName("청소년 전체 포함 여부 확인")
    void isAllTeenager() {
        // given
        int teenager = 4032;  // 중학생 + 고등학생

        // when & then
        assertThat(AgeGroupUtil.isAllTeenager(teenager)).isTrue();
        assertThat(AgeGroupUtil.isAllTeenager(448)).isFalse();  // 중학생만
        assertThat(AgeGroupUtil.isAllTeenager(3584)).isFalse(); // 고등학생만
    }

    @Test
    @DisplayName("비트마스크를 문자열로 변환")
    void toStringTest() {
        // given & when & then
        assertThat(AgeGroupUtil.toString(63)).isEqualTo("초등학생");
        assertThat(AgeGroupUtil.toString(4032)).isEqualTo("중학생, 고등학생");
        assertThat(AgeGroupUtil.toString(16383)).isEqualTo("전연령");
        assertThat(AgeGroupUtil.toString(0)).isEqualTo("없음");
    }

    @Test
    @DisplayName("Python 스크립트와 동일한 비트마스크 생성 확인")
    void pythonCompatibility() {
        // Python: TARGET_GROUP_MAPPING['초등학생'] = 63
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"초등학생"}))
                .isEqualTo(63);

        // Python: TARGET_GROUP_MAPPING['중학생'] = 448
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"중학생"}))
                .isEqualTo(448);

        // Python: TARGET_GROUP_MAPPING['고등학생'] = 3584
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"고등학생"}))
                .isEqualTo(3584);

        // Python: TARGET_BIT_MAPPING['성인'] = 4096
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"성인"}))
                .isEqualTo(4096);

        // Python: TARGET_BIT_MAPPING['시니어'] = 8192
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"시니어"}))
                .isEqualTo(8192);

        // Python: TARGET_GROUP_MAPPING['전체'] = 16383
        assertThat(AgeGroupUtil.encodeAgeGroups(new String[]{"전체"}))
                .isEqualTo(16383);
    }
}