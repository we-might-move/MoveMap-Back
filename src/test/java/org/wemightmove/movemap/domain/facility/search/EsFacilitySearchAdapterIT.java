package org.wemightmove.movemap.domain.facility.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse.FacilitySimpleInfo;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.search.index.FacilityDoc;
import org.wemightmove.movemap.global.search.support.EsContainerSupport;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * T6 — {@link EsFacilitySearchAdapter} 안전망 테스트.
 * <p>
 * 핵심은 (2) facility_subtype 부분매칭 회귀 가드다: T5 에서 {@code facility_subtype} 을
 * text(nori)+ngram+keyword 로 바꾼 이유는 레거시 {@code LIKE '%kw%'} 부분매칭(예: "골프" 검색 시
 * "골프연습장" 서브타입도 매치)을 미러링하기 위함이었다 — 매핑이 다시 순수 {@code keyword}(완전일치)로
 * 되돌아가면 이 테스트가 실패해 회귀를 잡는다.
 */
class EsFacilitySearchAdapterIT extends EsContainerSupport {

    private static final String INDEX = "facility_search";

    private static final long SWIM_FACILITY_ID = 101L;
    private static final long GOLF_SUBTYPE_FACILITY_ID = 102L; // 이름에는 "골프"가 없음 — subtype 매칭 전용
    private static final long GANGNAM_TENNIS_FACILITY_ID = 103L;
    private static final int FIXTURE_COUNT = 10;

    private static EsFacilitySearchAdapter adapter;

    @BeforeAll
    static void setUpIndexAndFixtures() throws IOException {
        createIndexFromMapping(INDEX, "es/mappings/facility.json");
        adapter = new EsFacilitySearchAdapter(ES_CLIENT);

        List<IdDoc<FacilityDoc>> docs = new ArrayList<>();
        docs.add(facility(SWIM_FACILITY_ID, "송파수영장", "AQUATIC", "수영장", "서울 송파구"));
        docs.add(facility(GOLF_SUBTYPE_FACILITY_ID, "한강체육공원", "LEISURE", "골프장", "서울 영등포구"));
        docs.add(facility(GANGNAM_TENNIS_FACILITY_ID, "강남테니스장", "BALL_GAME", "테니스", "서울 강남구"));
        docs.add(facility(104L, "마포축구공원", "BALL_GAME", "축구", "서울 마포구"));
        docs.add(facility(105L, "송파요가센터", "FITNESS", "요가", "서울 송파구"));
        docs.add(facility(106L, "강동체육관", "COMPLEX", "종합", "서울 강동구"));
        docs.add(facility(107L, "노원배드민턴장", "BALL_GAME", "배드민턴", "서울 노원구"));
        docs.add(facility(108L, "은평필라테스", "FITNESS", "필라테스", "서울 은평구"));
        docs.add(facility(109L, "종로탁구장", "BALL_GAME", "탁구", "서울 종로구"));
        docs.add(facility(110L, "용산댄스홀", "DANCE", "댄스", "서울 용산구"));

        bulkIndex(INDEX, docs);
    }

    private static IdDoc<FacilityDoc> facility(
            long id, String name, String facilityType, String facilitySubtype, String address) {
        return new IdDoc<>(id, FacilityDoc.of(id, name, facilityType, facilitySubtype, address, Instant.now()));
    }

    /** (1) Intent(D12): "수영" 검색 → "…수영장" 시설이 결과에 포함(포함 여부만 검증, 동등성 아님). */
    @Test
    void intent_swimKeyword_includesSwimFacility() {
        FacilitySimpleListResponse response = adapter.search("수영");

        Set<Long> ids = response.facilities().stream().map(FacilitySimpleInfo::id).collect(Collectors.toSet());
        assertThat(ids).contains(SWIM_FACILITY_ID);
    }

    /**
     * (2) facility_subtype 부분매칭 회귀 가드(T5 핵심 수정 대상): 이름에 "골프"가 없는 시설이라도
     * facility_subtype="골프장" 이면 "골프" 검색에 매치되어야 한다 — subtype 이 analyzed text 로
     * 매칭되지, exact term(keyword) 으로 매칭되는 게 아님을 증명한다.
     */
    @Test
    void facilitySubtypeSubstringMatch_golfSubtype_matchesEvenWithoutGolfInName() {
        FacilitySimpleListResponse response = adapter.search("골프");

        Set<Long> ids = response.facilities().stream().map(FacilitySimpleInfo::id).collect(Collectors.toSet());
        assertThat(ids).contains(GOLF_SUBTYPE_FACILITY_ID);
    }

    /** (5) 저-카디널리티 선택도: "골프"는 10건 중 subtype 매치 1건에만 매치되어야 한다(전체 반환 아님). */
    @Test
    void selectivity_golfKeyword_matchesOnlyExpectedOne_notEverything() {
        FacilitySimpleListResponse response = adapter.search("골프");

        Set<Long> ids = response.facilities().stream().map(FacilitySimpleInfo::id).collect(Collectors.toSet());
        assertThat(ids).containsExactly(GOLF_SUBTYPE_FACILITY_ID);
    }

    /** (3) 인젝션 안전(GLOBAL §9): query_string 흉내 문자열은 리터럴 텍스트로만 처리되어야 한다. */
    @Test
    void injectionSafe_queryStringLikeKeyword_isTreatedAsLiteral_noError_noFullIndexReturn() {
        String nonsense = "name:(x) OR _exists_:*";

        assertThatCode(() -> adapter.search(nonsense)).doesNotThrowAnyException();

        FacilitySimpleListResponse response = adapter.search(nonsense);
        // 픽스처 어디에도 이 리터럴 토큰 조합이 없으므로 매치가 없어야 한다(전체 10건 반환 X).
        assertThat(response.facilities()).isEmpty();
    }

    @Test
    void injectionSafe_wildcardLikeKeyword_isTreatedAsLiteral_bounded() {
        String wildcardish = "강남*";

        assertThatCode(() -> adapter.search(wildcardish)).doesNotThrowAnyException();

        FacilitySimpleListResponse response = adapter.search(wildcardish);
        // "강남" 토큰을 이름에 포함하는 시설(강남테니스장)만 매치 — 전체 10건이 아님.
        Set<Long> ids = response.facilities().stream().map(FacilitySimpleInfo::id).collect(Collectors.toSet());
        assertThat(ids).containsExactly(GANGNAM_TENNIS_FACILITY_ID);
        assertThat(response.facilities().size()).isLessThan(FIXTURE_COUNT);
    }

    /** 계약: ES 문서({@link FacilityDoc})가 아니라 {@link FacilitySimpleInfo} 로 정확히 매핑되어야 한다. */
    @Test
    void contract_responseFieldsMapCorrectlyToDto() {
        FacilitySimpleListResponse response = adapter.search("수영");

        FacilitySimpleInfo swim = response.facilities().stream()
                .filter(f -> f.id() == SWIM_FACILITY_ID)
                .findFirst()
                .orElseThrow();

        assertThat(swim.name()).isEqualTo("송파수영장");
        assertThat(swim.facilityType()).isEqualTo(FacilityType.AQUATIC.getName());
        assertThat(swim.facilitySubtype()).isEqualTo("수영장");
        assertThat(swim.address()).isEqualTo("서울 송파구");
    }
}
