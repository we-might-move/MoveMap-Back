package org.wemightmove.movemap.domain.facility.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestSupport;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/facility-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@Testcontainers
class FacilityControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Double offset = 0.01;
    private final Double southWestLat = 37.51720000 - offset;
    private final Double northEastLat = 37.51720000 + offset;
    private final Double southWestLng = 127.04730000 - offset;
    private final Double northEastLng = 127.04730000 + offset;

    @BeforeEach
    void setUp() {
        // SecurityContext에 Mock 사용자 등록
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.getEmail()).thenReturn("parent1@test.com");
        when(mockMember.getNickname()).thenReturn("테스트부모1");
        when(mockMember.getRole()).thenReturn(RoleType.PARENT);
        when(mockMember.getRegionCode()).thenReturn("11680");

        CustomUserDetails userDetails = new CustomUserDetails(mockMember);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 북마크 추가
    @Test
    void 사용자는_시설_북마크를_추가할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/facilities/1/bookmarks"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 시설_아이디가_존재하지_않을_경우_예외가_발생한다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/facilities/100000000/bookmarks"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.FACILITY_NOT_FOUND.getMessage()));

        mockMvc.perform(delete("/facilities/100000000/bookmarks"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.FACILITY_NOT_FOUND.getMessage()));
    }

    @Test
    void 이미_북마크한_시설일_경우_예외가_발생한다() throws Exception {
        //given
        mockMvc.perform(post("/facilities/1/bookmarks"))
                .andExpect(status().isNoContent());
        //when
        //then
        mockMvc.perform(post("/facilities/1/bookmarks"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ADDED_BOOKMARK.getMessage()));
    }

    // 북마크 삭제
    @Test
    void 사용자는_시설_북마크를_삭제할_수_있다() throws Exception {
        //given
        mockMvc.perform(post("/facilities/1/bookmarks"))
                .andExpect(status().isNoContent());
        //when
        //then
        mockMvc.perform(delete("/facilities/1/bookmarks"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 시설_아이디가_북마크_목록에_존재하지_않을_경우_예외가_발생한다() throws Exception {
        //given
        mockMvc.perform(post("/facilities/2/bookmarks"))
                .andExpect(status().isNoContent());
        //when
        //then
        mockMvc.perform(delete("/facilities/1/bookmarks"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_DELETED_BOOKMARK.getMessage()));
    }

    // 시설 마커 조회(초기)
    @Test
    void 사용자는_시설_탐색_지도에_접속하면_자신의_지역구로_바로_마커를_조회할_수_있다() throws Exception {
        //given
        /** 강남 위도/경도, radius 1000.0, LIMIT 100 인 상황의 결과
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE ST_DWithin(
         *               f.location::geography,
         *               ST_SetSRID(ST_MakePoint(127.04730000, 37.51720000), 4326)::geography,
         *               1000.0
         *       )
         * ORDER BY ST_Distance(
         *                  f.location::geography,
         *                  ST_SetSRID(ST_MakePoint(127.04730000, 37.51720000), 4326)::geography
         *          ) ASC
         * LIMIT 100;
         */
        //when
        //then
        mockMvc.perform(get("/facilities/markers/initial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(7));
    }

    // 시설 마커 조회(뷰포트 + 검색)

    /*
    조회되는 경우
     */
    @Test
    void 모든_값을_포함해서_조회해도_정상적으로_조회가_된다() throws Exception {
        //given
        String keyword = "강남";
        String city = "서울특별시";
        String district = "강남구";
        boolean isVoucher = false;
        int maxResults = 100;

        /**
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE 1=1
         *   AND f.latitude BETWEEN 37.50720000 AND 37.52720000
         *   AND f.longitude BETWEEN 127.03730000 AND 127.05730000
         *   AND ((f.region_cd LIKE 11680 || '%' AND f.facility_type = ANY(ARRAY['BALL_GAME', 'FITNESS', 'DANCE', 'AQUATIC', 'LEISURE', 'COMPLEX', 'ETC'])) OR (f.name ILIKE '%강남%'))
         * ORDER BY f.id DESC LIMIT 100;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&keyword=").append(keyword)
                .append("&city=").append(city)
                .append("&district=").append(district)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&maxResults=").append(maxResults)
                .append("&facilityTypes=").append("BALL_GAME")
                .append("&facilityTypes=").append("FITNESS")
                .append("&facilityTypes=").append("DANCE")
                .append("&facilityTypes=").append("AQUATIC")
                .append("&facilityTypes=").append("LEISURE")
                .append("&facilityTypes=").append("COMPLEX")
                .append("&facilityTypes=").append("ETC").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(8))
                .andExpect(jsonPath("$.markers[0].facilityId").value(18460))
                .andExpect(jsonPath("$.markers[1].facilityId").value(17799));
    }

    @Test
    void 뷰포트_좌표만_입력해도_정상적으로_조회된다() throws Exception {
        /**
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE 1=1
         *   AND f.latitude BETWEEN 37.50720000 AND 37.52720000
         *   AND f.longitude BETWEEN 127.03730000 AND 127.05730000
         *   AND ((f.region_cd LIKE 11680 || '%' AND f.facility_type = ANY(ARRAY['BALL_GAME', 'FITNESS', 'DANCE', 'AQUATIC', 'LEISURE', 'COMPLEX', 'ETC'])) OR (f.name ILIKE '%강남%'))
         * ORDER BY f.id DESC LIMIT 100;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markers[0].facilityId").value(17799))
                .andExpect(jsonPath("$.markers[1].facilityId").value(18460));
    }

    @Test
    void 좌표와_검색어만_입력해도_정상적으로_조회된다() throws Exception {
        //given
        String keyword = "공원";

        /**
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE 1=1
         *   AND f.latitude BETWEEN 37.50720000 AND 37.52720000
         *   AND f.longitude BETWEEN 127.03730000 AND 127.05730000
         *   AND ((f.name ILIKE '%공원%'))
         * ORDER BY f.id DESC LIMIT 500
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&keyword=").append(keyword).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.markers[0].facilityId").value(18460))
                .andExpect(jsonPath("$.markers[1].facilityId").value(17799));
    }

    @Test
    void 좌표와_지역정보를_다르게_입력하면_조회되지_않는다() throws Exception {
        //given
        String city = "서울특별시";
        String district = "용산구";

        /**
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE 1=1
         *   AND f.latitude BETWEEN 37.50720000 AND 37.52720000
         *   AND f.longitude BETWEEN 127.03730000 AND 127.05730000
         *   AND ((f.region_cd LIKE '11170' || '%'))
         * ORDER BY f.id DESC LIMIT 500;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append(city)
                .append("&district=").append(district).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    void 좌표와_시설유형만_입력해도_정보가_조회된다() throws Exception {
        //given
        /**
         * SELECT
         *     f.id,
         *     f.latitude,
         *     f.longitude,
         *     f.facility_type,
         *     f.name
         * FROM facility f
         * WHERE 1=1
         *   AND f.latitude BETWEEN 37.50720000 AND 37.52720000
         *   AND f.longitude BETWEEN 127.03730000 AND 127.05730000
         *   AND ((f.facility_type = ANY(ARRAY['COMPLEX'])))
         * ORDER BY ST_DistanceSphere(
         *                  f.location,
         *                  ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326)
         *          ) ASC
         * LIMIT 500;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&facilityTypes=").append("COMPLEX").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.markers[0].facilityId").value(17799))
                .andExpect(jsonPath("$.markers[1].facilityId").value(18460));
    }

    @Test
    void 좌표와_여러_시설유형을_입력해도_정보가_조회된다() throws Exception {
        //given
        /*
        SELECT
            f.id,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.name
        FROM facility f
        WHERE 1=1
          AND f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
          AND ((f.facility_type = ANY(ARRAY['BALL_GAME', 'COMPLEX'])))
        ORDER BY ST_DistanceSphere(
                         f.location,
                         ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326)
                 ) ASC
        LIMIT 500;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&facilityTypes=").append("BALL_GAME")
                .append("&facilityTypes=").append("COMPLEX").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(8))
                .andExpect(jsonPath("$.markers[0].facilityId").value(17799))
                .andExpect(jsonPath("$.markers[1].facilityId").value(18460));
    }

    // (Viewport bbox) AND ((지역 AND 시설 타입 AND 바우처 타입) OR (검색어))
    @Test
    void 뷰포트_범위에_포함되고_필터링_조건을_모두_만족하면_검색어가_포함되지_않아도_조회된다() throws Exception {
        //given
        String keyword = "랜덤값";
        String city = "경상북도";
        String district = "문경시";
        boolean isVoucher = false;
        int maxResults = 100;

        /*
        SELECT
            f.id,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.name
        FROM facility f
        WHERE 1=1
          AND f.latitude BETWEEN 36.57640000 AND 36.59640000
          AND f.longitude BETWEEN 128.17970000 AND 128.19970000
          AND ((f.region_cd LIKE 47280 || '%' AND f.facility_type = ANY(ARRAY['MARTIAL_ARTS'])) OR (f.name ILIKE '%랜덤값%'))
        ORDER BY f.id DESC LIMIT 100;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(36.59640000)
                .append("&northEastLng=").append(128.19970000)
                .append("&southWestLat=").append(36.57640000)
                .append("&southWestLng=").append(128.17970000)
                .append("&keyword=").append(keyword)
                .append("&city=").append(city)
                .append("&district=").append(district)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&maxResults=").append(maxResults)
                .append("&facilityTypes=").append("MARTIAL_ARTS").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.markers[0].facilityId").value(5099));

    }

    @Test
    void 뷰포트_범위에_포함되고_검색어_조건을_만족하면_포함되어_조회된다() throws Exception {
        //given
        String keyword = "공원";
        String city = "서울특별시";
        String district = "용산구";
        boolean isVoucher = false;
        int maxResults = 100;

        /*
        SELECT
            f.id,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.name
        FROM facility f
        WHERE 1=1
          AND f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
          AND ((f.region_cd LIKE '11170' || '%' AND f.facility_type = ANY(ARRAY['MARTIAL_ARTS'])) OR (f.name ILIKE '%공원%'))
        ORDER BY f.id DESC LIMIT 100;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&keyword=").append(keyword)
                .append("&city=").append(city)
                .append("&district=").append(district)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&maxResults=").append(maxResults)
                .append("&facilityTypes=").append("MARTIAL_ARTS").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.markers[0].facilityId").value(18460))
                .andExpect(jsonPath("$.markers[1].facilityId").value(17799));
    }

    @Test
    void 검색_조건이_있으면_최신순으로_조회된다() throws Exception {
        //given
        boolean isVoucher = false;
        int maxResults = 100;

        /*
        SELECT
            f.id,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.name
        FROM facility f
        WHERE 1=1
          AND f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
        ORDER BY f.id DESC LIMIT 100;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&maxResults=").append(maxResults).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(8))
                .andExpect(jsonPath("$.markers[0].facilityId").value(18460))
                .andExpect(jsonPath("$.markers[1].facilityId").value(17799));
    }

    @Test
    void 검색조건이_없으면_거리순으로_조회된다() throws Exception {
        //given
        /*
        SELECT
            f.id,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.name
        FROM facility f
        WHERE 1=1
          AND f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
        ORDER BY ST_DistanceSphere(
                         f.location,
                         ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326)
                 ) ASC
        LIMIT 500;
         */
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(8))
                .andExpect(jsonPath("$.markers[0].facilityId").value(17799))
                .andExpect(jsonPath("$.markers[1].facilityId").value(18460));
    }

    // 에러 관련 테스트
    @Test
    void 위도_경도_정보가_없으면_400에러가_발생한다() throws Exception {
        //given
        String url = new StringBuilder()
                .append("/facilities/markers").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 북동쪽보다_남서쪽_값이_크거나_같으면_500에러가_발생한다() throws Exception {
        //given
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(33.0)
                .append("&northEastLng=").append(132.0)
                .append("&southWestLat=").append(33.0)
                .append("&southWestLng=").append(132.0).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 검색어_글자수가_50자_초과이면_400에러가_발생한다() throws Exception {
        //given
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&keyword=").append("slkjglakjsdkljgskljgkajlgsjdkslgjlskdjgksldjglsdjlgkjsdljgklsdjgklsjlkgjkdjkgjklsdjglkjskljkfljgklsjgklfs").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 시도와_시군구의_짝이_맞지_않으면_400에러가_발생한다() throws Exception {
        //given
        String url = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append("경기도")
                .append("&district=").append("용산구").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("부모 지역(시/도)과 포함 지역(시/군/구)의 짝이 알맞지 않습니다."));
    }

    @Test
    void 시도와_시군구_값에_이상한_값을_넣으면_400에러가_발생한다() throws Exception {
        //given
        String url1 = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append("이상한 값").toString();

        String url2 = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&district=").append("이상한 값").toString();

        //when
        //then
        mockMvc.perform(get(url1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 지역(시/도)입니다"));

        mockMvc.perform(get(url2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 지역(시/군/구)입니다"));
    }

    // 시설 리스트 조회(초기)

    // 정상 작동
    @Test
    void 시설_탐색_맵에_들어가면_사용자의_지역구_정보를_기준으로_리스트가_응답된다() throws Exception {
        //given
        /*
        SELECT
            f.id,
            f.name,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.facility_subtype,
            f.address,
            f.is_voucher_available,
            ST_Distance(
                    f.location::geography,
                    ST_SetSRID(ST_MakePoint(127.04730000, 37.51720000), 4326)::geography
            ) AS distance_meters,
            COALESCE(AVG(fr.rating), 0) AS avg_rating,
            COUNT(fr.id) AS review_count,
            CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
        FROM facility f
                 LEFT JOIN facility_review fr ON f.id = fr.facility_id
                 LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = 1
        WHERE ST_DWithin(
                f.location::geography,
                ST_SetSRID(ST_MakePoint(127.04730000, 37.51720000), 4326)::geography,
                1000.0
              )
          AND f.region_cd LIKE 11680 || '%'
        GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type,
                 f.facility_subtype, f.address, f.is_voucher_available,
                 distance_meters, mf.id
        ORDER BY distance_meters ASC, f.id DESC
        LIMIT 40;
         */
        String url = new StringBuilder()
                .append("/facilities/list/initial").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities.length()").value(7))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));

    }

    // 시설 리스트 조회(뷰포트 + 검색)

    // 정상 작동
    @Test
    void 시설_탐색_맵에서_리스트를_조회할_때_모든_조건을_추가해도_정상_조회된다() throws Exception {
        //given
        String keyword = "골프";
        String city = "서울특별시";
        String district = "강남구";
        boolean isVoucher = false;
        Long cursor = 36231L;
        int size = 5;

        /*
        SELECT
            f.id,
            f.name,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.facility_subtype,
            f.address,
            f.is_voucher_available,
            NULL AS distance_meters,
            COALESCE(AVG(fr.rating), 0) AS avg_rating,
            COUNT(fr.id) AS review_count,
            CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
        FROM facility f
                 LEFT JOIN facility_review fr ON f.id = fr.facility_id
                 LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = 1
        WHERE f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
          AND ((f.region_cd LIKE 11680 || '%' AND f.facility_type = ANY(ARRAY['MARTIAL_ARTS']) AND f.id < 36231) OR (f.name ILIKE '%강남%'))
        GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type,
                 f.facility_subtype, f.address, f.is_voucher_available
                , mf.id
        ORDER BY f.id DESC
        LIMIT 5;
         */
        String url = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&keyword=").append(keyword)
                .append("&city=").append(city)
                .append("&district=").append(district)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&cursor=").append(cursor)
                .append("&size=").append(size)
                .append("&facilityTypes=").append("MARTIAL_ARTS").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.facilities[0].id").value(110));

    }

    @Test
    void 시설_리스트_조회에서_뷰포트_좌표만_입력해도_정상적으로_조회된다() throws Exception {
        //given
        Long cursor = 30000L;
        int size = 5;

        /*
        SELECT
            f.id,
            f.name,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.facility_subtype,
            f.address,
            f.is_voucher_available,
            ST_Distance(
                    f.location::geography,
                    ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326)::geography
            ) AS distance_meters,
            COALESCE(AVG(fr.rating), 0) AS avg_rating,
            COUNT(fr.id) AS review_count,
            CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
        FROM facility f
                 LEFT JOIN facility_review fr ON f.id = fr.facility_id
                 LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = 1
        WHERE f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
          AND ((f.id < 30000))
        GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type,
            f.facility_subtype, f.address, f.is_voucher_available
               , distance_meters
               , mf.id
        ORDER BY distance_meters ASC, f.id DESC
        LIMIT 5;
         */
        String url = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&cursor=").append(cursor)
                .append("&size=").append(size).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(15160))
                .andExpect(jsonPath("$.facilities[0].id").value(17799));

    }

    @Test
    void 시설_리스트_조회에서_필터링_조건의_존재_여부에_따라_정렬이_다르게_조회된다() throws Exception {
        //given
        int size = 5;
        String city = "경상북도";
        String district = "문경시";
        boolean isVoucher = false;

        /*
        SELECT
            f.id,
            f.name,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.facility_subtype,
            f.address,
            f.is_voucher_available,
            ST_Distance(
                    f.location::geography,
                    ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326)::geography
            ) AS distance_meters,
            COALESCE(AVG(fr.rating), 0) AS avg_rating,
            COUNT(fr.id) AS review_count,
            CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
        FROM facility f
                 LEFT JOIN facility_review fr ON f.id = fr.facility_id
                 LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = 1
        WHERE f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
        GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type,
            f.facility_subtype, f.address, f.is_voucher_available
               , distance_meters
               , mf.id
        ORDER BY distance_meters ASC, f.id DESC
        LIMIT 5;
         */
        String url1 = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(36.59640000)
                .append("&northEastLng=").append(128.19970000)
                .append("&southWestLat=").append(36.57640000)
                .append("&southWestLng=").append(128.17970000)
                .append("&size=").append(size).toString();

        /*
        SELECT
            f.id,
            f.name,
            f.latitude,
            f.longitude,
            f.facility_type,
            f.facility_subtype,
            f.address,
            f.is_voucher_available,
            NULL AS distance_meters,
            COALESCE(AVG(fr.rating), 0) AS avg_rating,
            COUNT(fr.id) AS review_count,
            CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
        FROM facility f
                 LEFT JOIN facility_review fr ON f.id = fr.facility_id
                 LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = 1
        WHERE f.latitude BETWEEN 37.50720000 AND 37.52720000
          AND f.longitude BETWEEN 127.03730000 AND 127.05730000
          AND ((f.region_cd LIKE 11680 || '%' AND f.facility_type = ANY(ARRAY['MARTIAL_ARTS'])))
        GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type,
                 f.facility_subtype, f.address, f.is_voucher_available
                , mf.id
        ORDER BY f.id DESC
        LIMIT 5;
         */
        String url2 = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(36.59640000)
                .append("&northEastLng=").append(128.19970000)
                .append("&southWestLat=").append(36.57640000)
                .append("&southWestLng=").append(128.17970000)
                .append("&city=").append(city)
                .append("&district=").append(district)
                .append("&isVoucherAvailable=").append(isVoucher)
                .append("&size=").append(size)
                .append("&facilityTypes=").append("MARTIAL_ARTS").toString();

        //then
        mockMvc.perform(get(url1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(14418))
                .andExpect(jsonPath("$.facilities[0].id").value(5623));

        mockMvc.perform(get(url2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()))
                .andExpect(jsonPath("$.facilities[0].id").value(5099));

    }

    // 예외 처리
    @Test
    void 위경도_데이터가_없으면_예외가_발생한다() throws Exception {
        //given
        Long cursor = 30000L;
        int size = 5;

        String url = new StringBuilder()
                .append("/facilities/list?")
                .append("cursor=").append(cursor)
                .append("&size=").append(size).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 북동쪽_위경도가_남서쪽_위경도보다_작거나_같으면_예외가_발생한다() throws Exception {
        //given
        Long cursor = 30000L;
        int size = 5;

        String url = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(33.0)
                .append("&northEastLng=").append(132.0)
                .append("&southWestLat=").append(33.0)
                .append("&southWestLng=").append(132.0)
                .append("&cursor=").append(cursor)
                .append("&size=").append(size).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 요청에_시도와_시군구가_같이_있지_않으면_예외가_발생한다() throws Exception {
        //given
        Long cursor = 30000L;
        int size = 5;
        String district = "강남구";

        String url = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(33.0)
                .append("&northEastLng=").append(132.0)
                .append("&southWestLat=").append(33.0)
                .append("&southWestLng=").append(132.0)
                .append("&district=").append(district)
                .append("&cursor=").append(cursor)
                .append("&size=").append(size).toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 시설_리스트_탐색_시도와_시군구의_짝이_맞지_않으면_400에러가_발생한다() throws Exception {
        //given
        String url = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append("경기도")
                .append("&district=").append("용산구").toString();

        //when
        //then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("부모 지역(시/도)과 포함 지역(시/군/구)의 짝이 알맞지 않습니다."));
    }

    /**
     * FIXME: 이거 지금 로직이 이상함
     * - getRegionCode에서 분기가 되어야 하는데, Request 에서 예외 처리를 해서 getRegionCode가 안 먹힘
     */
//    @Test
    void 시설_리스트_탐색_시도와_시군구_값에_이상한_값을_넣으면_400에러가_발생한다() throws Exception {
        //given
        String url1 = new StringBuilder()
                .append("/facilities/list")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append("이상한 값")
                .append("&district=").append("용산구").toString();


        String url2 = new StringBuilder()
                .append("/facilities/markers")
                .append("?northEastLat=").append(northEastLat)
                .append("&northEastLng=").append(northEastLng)
                .append("&southWestLat=").append(southWestLat)
                .append("&southWestLng=").append(southWestLng)
                .append("&city=").append("서울특별시")
                .append("&district=").append("이상한 값").toString();

        //when
        //then
        mockMvc.perform(get(url1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 지역(시/도)입니다"));

        mockMvc.perform(get(url2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 지역(시/군/구)입니다"));
    }

    // 시설 상세 조회

    // 정상 처리

    /**
     * FIXME: 사용자에게 보여줄 값을 줘야 하므로 BALL_GAME 값이 아닌 구기로 값 넘겨줘야 함
     */
    @Test
    void 시설_상세_조회를_하면_정상_응답이_온다() throws Exception {
        Long facilityId = 2L;

        String url = new StringBuilder()
                .append("/facilities/").append(facilityId)
                .append("?lat=").append("37.5219")
                .append("&lng=").append("127.1230").toString();

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("(평창)청성골프타운(임대)"))
                .andExpect(jsonPath("$.latitude").value(37.37736263))
                .andExpect(jsonPath("$.longitude").value(128.38555520))
                .andExpect(jsonPath("$.facilityType").value(FacilityType.BALL_GAME.name()))
                .andExpect(jsonPath("$.facilitySubtype").value("골프연습장"))
                .andExpect(jsonPath("$.address").value("강원특별자치도 평창군 평창읍 살구실길 53"))
                .andExpect(jsonPath("$.isVoucherAvailable").value("false"))
                .andExpect(jsonPath("$.distanceMeters").isNotEmpty())
                .andExpect(jsonPath("$.avgRating").value(0))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.isBookmarked").value("false"));
    }

    // 예외 처리
    @Test
    void 존재하지_않는_시설_아이디를_입력하면_예외가_발생한다() throws Exception {
        Long facilityId = 1294858794948L;

        String url = new StringBuilder()
                .append("/facilities/").append(facilityId)
                .append("?lat=").append("37.5219")
                .append("&lng=").append("127.1230").toString();

        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.FACILITY_NOT_FOUND.getMessage()));
    }

    // 검색 키워드로 시설 리스트 검색

    // 정상 응답
    @Test
    void 시설명과_시설_서브_타입으로_시설_리스트를_조회할_수_있다() throws Exception {
        String keyword = "축구";

        String url = new StringBuilder()
                .append("/facilities/search?")
                .append("keyword=").append(keyword).toString();

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities[0].id").value(718))
                .andExpect(jsonPath("$.facilities[0].name").value("다덕축구장"))
                .andExpect(jsonPath("$.facilities[0].facilityType").value(FacilityType.BALL_GAME.getName()))
                .andExpect(jsonPath("$.facilities[0].facilitySubtype").value("축구장"))
                .andExpect(jsonPath("$.facilities[0].address").value("봉성면 우곡리 534 외"));

    }

    /**
     * FIXME: 이것도 성공시키도록 고치기
     */
//    @Test
    void 공백무관_시설명과_시설_서브_타입으로_시설_리스트를_조회할_수_있다() throws Exception {
        String keyword = "축 구";

        String url = new StringBuilder()
                .append("/facilities/search?")
                .append("keyword=").append(keyword).toString();

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities[0].id").value(718))
                .andExpect(jsonPath("$.facilities[0].name").value("다덕축구장"))
                .andExpect(jsonPath("$.facilities[0].facilityType").value(FacilityType.BALL_GAME.getName()))
                .andExpect(jsonPath("$.facilities[0].facilitySubtype").value("축구장"))
                .andExpect(jsonPath("$.facilities[0].address").value("봉성면 우곡리 534 외"));

    }

    // 예외 응답 - 없네?

    // 시설 리뷰 등록

    // 정상 응답
    @Test
    void 시설_리뷰를_등록할_수_있다() throws Exception {
        Long facilityId = 3L;

        FacilityReviewRequest request = new FacilityReviewRequest(5, "잘 이용했습니다.", "위치도 가깝고 좋습니다.");

        String url = new StringBuilder()
                .append("/facilities/").append(facilityId).append("/reviews").toString();

        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    // 예외 응답
    @Test
    void 이미_리뷰를_남긴_시설에_리뷰를_남기려고_하면_예외가_발생한다() throws Exception {
        Long facilityId = 4L;

        FacilityReviewRequest request = new FacilityReviewRequest(5, "잘 이용했습니다.", "위치도 가깝고 좋습니다.");

        String url = new StringBuilder()
                .append("/facilities/").append(facilityId).append("/reviews").toString();

        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 리뷰를 작성한 시설입니다."));
    }

    // 시설 리뷰 리스트 조회

    // 정상 응답
    @Test
    void 검색_키워드_있어도_조회_가능하다() throws Exception {
        // given
        String url = new StringBuilder()
                .append("/facilities/reviews?")
                .append("lat=").append("34.68897451")
                .append("&lng=").append("126.70722660")
                .append("&city=").append("서울특별시")
                .append("&district=").append("강남구")
                .append("&keyword=").append("골프")
                .append("&cursor=").append(0)
                .append("&size=").append(20).toString();

        // when
        // then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].facilityId").value(1))
                .andExpect(jsonPath("$.reviews[0].facilityName").value("(구)성화대 골프연습장"))
                .andExpect(jsonPath("$.reviews[0].facilityType").value(FacilityType.BALL_GAME.name()))
                .andExpect(jsonPath("$.reviews[0].address").value("전라남도 강진군 성전면 송계로 897-10"))
                .andExpect(jsonPath("$.reviews[0].distance").isNotEmpty())
                .andExpect(jsonPath("$.reviews[0].reviewId").value(1))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].reviewTitle").value("전라남도 골프연습장 리뷰"))
                .andExpect(jsonPath("$.reviews[0].reviewContent").value("골프 연습장 좋네요."))
                .andExpect(jsonPath("$.reviews[0].reviewerNickname").value("테스트부모1"))
                .andExpect(jsonPath("$.reviews[0].cursor").value(1));
    }

    @Test
    void 검색_키워드_없이도_조회_가능하다() throws Exception {
        // given
        String url = new StringBuilder()
                .append("/facilities/reviews?")
                .append("lat=").append("34.68897451")
                .append("&lng=").append("126.70722660")
                .append("&city=").append("서울특별시")
                .append("&district=").append("강남구")
                .append("&cursor=").append(0)
                .append("&size=").append(20).toString();

        // when
        // then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].facilityId").value(1))
                .andExpect(jsonPath("$.reviews[0].facilityName").value("(구)성화대 골프연습장"))
                .andExpect(jsonPath("$.reviews[0].facilityType").value(FacilityType.BALL_GAME.name()))
                .andExpect(jsonPath("$.reviews[0].address").value("전라남도 강진군 성전면 송계로 897-10"))
                .andExpect(jsonPath("$.reviews[0].distance").isNotEmpty())
                .andExpect(jsonPath("$.reviews[0].reviewId").value(1))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].reviewTitle").value("전라남도 골프연습장 리뷰"))
                .andExpect(jsonPath("$.reviews[0].reviewContent").value("골프 연습장 좋네요."))
                .andExpect(jsonPath("$.reviews[0].reviewerNickname").value("테스트부모1"))
                .andExpect(jsonPath("$.reviews[0].cursor").value(1));
    }

    @Test
    void 위경도_데이터가_지역코드_데이터보다_우선_적용되어_조회_가능하다() throws Exception {
        // given
        String url = new StringBuilder()
                .append("/facilities/reviews?")
                .append("lat=").append("37.51720000")
                .append("&lng=").append("127.04730000")
                .append("&city=").append("전라남도")
                .append("&district=").append("강진군")
                .append("&cursor=").append(0)
                .append("&size=").append(20).toString();

        // when
        // then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].facilityId").value(10))
                .andExpect(jsonPath("$.reviews[0].facilityName").value("강남스포츠문화센터골프연습장"))
                .andExpect(jsonPath("$.reviews[0].facilityType").value(FacilityType.BALL_GAME.name()))
                .andExpect(jsonPath("$.reviews[0].address").value("강남구 수서동718"))
                .andExpect(jsonPath("$.reviews[0].distance").isNotEmpty())
                .andExpect(jsonPath("$.reviews[0].reviewId").value(2))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].reviewTitle").value("강남 골프연습장 리뷰"))
                .andExpect(jsonPath("$.reviews[0].reviewContent").value("골프 연습장 좋네요."))
                .andExpect(jsonPath("$.reviews[0].reviewerNickname").value("테스트부모1"))
                .andExpect(jsonPath("$.reviews[0].cursor").value(2));
    }

    @Test
    void 시설_리뷰_조회_시에_무한_스크롤로_동작한다() throws Exception {
        String url = new StringBuilder()
                .append("/facilities/reviews?")
                .append("lat=").append("37.51720000")
                .append("&lng=").append("127.04730000")
                .append("&city=").append("전라남도")
                .append("&district=").append("강진군")
                .append("&cursor=").append(0)
                .append("&size=").append(20).toString();

        // when
        // then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").isBoolean())
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    // 예외 응답
    @Test
    void 현재_위경도_지역구_둘_다_없으면_예외가_발생한다() throws Exception {
        String url = new StringBuilder()
                .append("/facilities/reviews?")
                .append("cursor=").append(0)
                .append("&size=").append(20).toString();

        // when
        // then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }
}