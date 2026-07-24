package org.wemightmove.movemap.domain.program.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse.ProgramSimpleItem;
import org.wemightmove.movemap.global.search.support.EsContainerSupport;
import org.wemightmove.movemap.global.search.index.ProgramDoc;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * T6 — {@link EsProgramSearchAdapter} 안전망 테스트.
 * <p>
 * GLOBAL §12: 옛(DB) 결과와의 동등성 회귀는 만들지 않는다. intent(포함 여부)/계약(DTO 필드·페이징)/
 * 인젝션 안전/선택도(bool minimumShouldMatch) 위주.
 * <p>
 * 인덱스는 어댑터가 하드코딩한 alias 이름({@code program_search})으로 <b>직접</b> 생성한다(실제
 * {@code es/mappings/program.json} 매핑 그대로) — ES 입장에서 alias 든 concrete 인덱스든 검색 동작은
 * 동일하므로 별도 alias 배선 없이 어댑터를 그대로 재사용할 수 있다.
 */
class EsProgramSearchAdapterIT extends EsContainerSupport {

    private static final String INDEX = "program_search";

    // 기본 픽스처: intent/injection/selectivity 검증용 소규모(10건) 세트.
    private static final long SWIM_PROGRAM_ID = 1L;
    private static final long SWIM_KIDS_PROGRAM_ID = 2L;

    // 페이지네이션 검증용 25건(공통 키워드 "페이지네이션"만 포함, 기본 픽스처와 겹치지 않음).
    private static final long PAGE_FIXTURE_BASE_ID = 1000L;
    private static final int PAGE_FIXTURE_COUNT = 25;

    private static EsProgramSearchAdapter adapter;

    @BeforeAll
    static void setUpIndexAndFixtures() throws IOException {
        createIndexFromMapping(INDEX, "es/mappings/program.json");
        adapter = new EsProgramSearchAdapter(ES_CLIENT);

        List<IdDoc<ProgramDoc>> docs = new ArrayList<>();
        docs.add(program(SWIM_PROGRAM_ID, "수영강습", "강남스포츠센터", "수영장", "서울 강남구"));
        docs.add(program(SWIM_KIDS_PROGRAM_ID, "유아수영교실", "송파구민센터", "수영장", "서울 송파구"));
        docs.add(program(3L, "요가교실", "강남스포츠센터", "요가", "서울 강남구"));
        docs.add(program(4L, "필라테스클래스", "서초구민센터", "필라테스", "서울 서초구"));
        docs.add(program(5L, "탁구교실", "마포구민센터", "탁구", "서울 마포구"));
        docs.add(program(6L, "배드민턴강습", "마포구민센터", "배드민턴", "서울 마포구"));
        docs.add(program(7L, "축구클래스", "강남스포츠센터", "축구", "서울 강남구"));
        docs.add(program(8L, "농구교실", "강동구민센터", "농구", "서울 강동구"));
        docs.add(program(9L, "줄넘기교실", "강동구민센터", "줄넘기", "서울 강동구"));
        docs.add(program(10L, "댄스스포츠", "노원구민센터", "댄스", "서울 노원구"));

        for (int i = 0; i < PAGE_FIXTURE_COUNT; i++) {
            long id = PAGE_FIXTURE_BASE_ID + i;
            docs.add(program(id, "페이지네이션프로그램" + i, "테스트구민센터", "테스트종목", "서울 테스트구"));
        }

        bulkIndex(INDEX, docs);
    }

    private static IdDoc<ProgramDoc> program(
            long id, String name, String facilityName, String facilitySubtype, String address) {
        return new IdDoc<>(id, ProgramDoc.of(id, name, facilityName, facilitySubtype, address, Instant.now()));
    }

    private static ProgramSearchByKeywordRequest request(String keyword, Integer size, Long cursor) {
        return new ProgramSearchByKeywordRequest(keyword, size, cursor);
    }

    /** (1) Intent(D12): "수영" 검색 → 수영 프로그램이 결과에 포함(동등성이 아니라 포함 여부만 검증). */
    @Test
    void intent_swimKeyword_includesSwimPrograms() {
        ProgramSimpleListResponse response = adapter.search(request("수영", 20, null));

        Set<Long> ids = response.programs().stream().map(ProgramSimpleItem::id).collect(Collectors.toSet());
        assertThat(ids).contains(SWIM_PROGRAM_ID, SWIM_KIDS_PROGRAM_ID);
    }

    /**
     * (5) 저-카디널리티 선택도 가드: "수영"은 10건 중 2건에만 매치되어야 한다.
     * bool.should(...).minimumShouldMatch(1) 이 사실상 OR-매치이지 전체 반환이 아님을 증명한다(T5 리뷰 지적).
     */
    @Test
    void selectivity_swimKeyword_matchesOnlyExpectedTwo_notEverything() {
        ProgramSimpleListResponse response = adapter.search(request("수영", 20, null));

        Set<Long> ids = response.programs().stream().map(ProgramSimpleItem::id).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(SWIM_PROGRAM_ID, SWIM_KIDS_PROGRAM_ID);
    }

    /** (3) 인젝션 안전(GLOBAL §9): query_string 흉내 문자열도 리터럴 텍스트로만 처리되어 에러 없이, 전체 반환도 아니어야 한다. */
    @Test
    void injectionSafe_queryStringLikeKeyword_isTreatedAsLiteral_noError_noFullIndexReturn() {
        String nonsense = "name:(x) OR _exists_:*";

        assertThatCode(() -> adapter.search(request(nonsense, 20, null))).doesNotThrowAnyException();

        ProgramSimpleListResponse response = adapter.search(request(nonsense, 20, null));
        // 픽스처 어디에도 이 리터럴 토큰 조합이 없으므로 매치가 없어야 한다(전체 35건 반환 X).
        assertThat(response.programs()).isEmpty();
    }

    @Test
    void injectionSafe_wildcardLikeKeyword_isTreatedAsLiteral_bounded() {
        String wildcardish = "강남*";

        assertThatCode(() -> adapter.search(request(wildcardish, 100, null))).doesNotThrowAnyException();

        ProgramSimpleListResponse response = adapter.search(request(wildcardish, 100, null));
        // wildcard 로 파싱됐다면 전체(35건)에 가깝게 반환될 위험이 있으나, match 텍스트 분석으로는
        // "강남" 토큰을 포함하는 facility_name(강남스포츠센터) 보유 3건(id 1,3,7)만 매치되어야 한다
        // — 전체 인덱스가 아님을 증명(선택도 가드 겸용).
        Set<Long> ids = response.programs().stream().map(ProgramSimpleItem::id).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(1L, 3L, 7L);
        assertThat(response.programs().size()).isLessThan(PAGE_FIXTURE_COUNT + 10);
    }

    /** (4) 계약 + 페이지네이션: size=10 으로 25건을 조회 시 1·2페이지가 disjoint, id 오름차순, DTO 필드 정확. */
    @Test
    void contractAndPagination_page1AndPage2AreDisjointAscendingAndCoverMatchedSet() {
        ProgramSimpleListResponse page1 = adapter.search(request("페이지네이션", 10, null));

        assertThat(page1.currentSize()).isEqualTo(10);
        assertThat(page1.programs()).hasSize(10);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page1.nextCursor()).isNotNull();

        List<Long> page1Ids = page1.programs().stream().map(ProgramSimpleItem::id).toList();
        assertThat(page1Ids).isSorted();
        assertThat(page1.nextCursor()).isEqualTo(page1Ids.get(page1Ids.size() - 1));

        ProgramSimpleListResponse page2 = adapter.search(request("페이지네이션", 10, page1.nextCursor()));

        assertThat(page2.currentSize()).isEqualTo(10);
        assertThat(page2.hasNext()).isTrue();

        List<Long> page2Ids = page2.programs().stream().map(ProgramSimpleItem::id).toList();
        assertThat(page2Ids).isSorted();

        // disjoint
        Set<Long> intersection = new HashSet<>(page1Ids);
        intersection.retainAll(page2Ids);
        assertThat(intersection).isEmpty();

        // id 오름차순 연속성: page2 의 최솟값이 page1 의 최댓값보다 커야 함
        assertThat(page2Ids.get(0)).isGreaterThan(page1Ids.get(page1Ids.size() - 1));

        // DTO 계약: 필드가 픽스처 값과 정확히 매핑되는지 첫 항목으로 확인
        ProgramSimpleItem first = page1.programs().get(0);
        assertThat(first.facilityName()).isEqualTo("테스트구민센터");
        assertThat(first.facilitySubtype()).isEqualTo("테스트종목");
        assertThat(first.address()).isEqualTo("서울 테스트구");
        assertThat(first.programName()).startsWith("페이지네이션프로그램");
    }
}
