package org.wemightmove.movemap.domain.program.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.program.dto.request.*;
import org.wemightmove.movemap.domain.program.dto.response.*;
import org.wemightmove.movemap.domain.program.service.ProgramCommandService;
import org.wemightmove.movemap.domain.program.service.ProgramQueryService;
import org.wemightmove.movemap.domain.program.service.ProgramReviewCommandService;
import org.wemightmove.movemap.domain.program.service.ProgramReviewQueryService;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.WeekDayType;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.util.List;

@RestController
@Tag(name = "Program")
@RequiredArgsConstructor
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramQueryService programQueryService;
    private final ProgramReviewCommandService programReviewCommandService;
    private final ProgramReviewQueryService programReviewQueryService;
    private final ProgramCommandService programCommandService;

    @Operation(summary = "프로그램 북마크 추가")
    @PostMapping("/{id}/bookmarks")
    public ResponseEntity<Void> bookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long programId) {
        programCommandService.addBookmarkProgram(member.getId(), programId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로그램 북마크 제거")
    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> deleteBookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long programId) {
        programCommandService.deleteBookmarkProgram(member.getId(), programId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로그램 마커 조회(초기)")
    @GetMapping("/markers/initial")
    public ResponseEntity<ProgramMarkerResponse> getInitialMarkers(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        ProgramMarkerResponse response = programQueryService.getMarkers(member.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로그램 마커 조회 (Viewport + 검색)",
            description = """
            지도에 표시할 프로그램 마커 조회 (좌표별 그룹핑)
            
            Viewport 좌표 (필수)
            - northEastLat, northEastLng : 상단 우측 모서리
            - southWestLat, southWestLng : 하단 좌측 모서리
            
            마커 그룹핑
            - 같은 좌표에 여러 프로그램 → 하나의 마커로 표시
            - programCount: 해당 위치의 프로그램 개수
            - representativeName: 대표 프로그램명 (최소값)
            
            정렬 방식
            - 검색 조건 없음: 거리순 (Viewport 중심 기준)
            - 검색 조건 있음: 프로그램 개수 많은 순 (program_count DESC)
            
            검색 조건
            - keyword: 프로그램명 검색
            - city, district: 지역 필터
            - facilityTypes: 시설 유형 (다중 선택)
            - weekDayTypes: 요일 필터 (다중 선택, 비트마스크)
            - minPrice, maxPrice: 가격 범위 (둘 다 0이면 무료만)
            - minAge, maxAge: 연령 범위 (비트마스크)
            
            검색 조건 조합 방식
            (Viewport 필터) AND (
                (keyword 조건) OR 
                (city + district + facilityTypes + price + weekday + age 필터)
            )
            """
    )
    @GetMapping("/markers")
    public ResponseEntity<ProgramMarkerResponse> getMarkers(
            @AuthenticationPrincipal CustomUserDetails member,
            @ModelAttribute ProgramMarkerRequest request,
            @RequestParam(value = "facilityTypes", required = false) List<FacilityType> facilityTypes,
            @RequestParam(value = "weekDayTypes", required = false) List<WeekDayType> weekDayTypes
    ) {
        ProgramMarkerResponse response = programQueryService.getMarkersBySearch(request, facilityTypes, weekDayTypes, member.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로그램 리스트 조회 (사용자 지역 기반)",
            description = """
            사용자가 등록한 지역 기반으로 프로그램 조회
            
            조회 방식
            - 사용자 regionCode 기반 필터링
            - 최신순 정렬 (id DESC)
            - 거리 계산 없음 (distance = null)
            
            페이지네이션 (커서 기반)
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor 값을 cursor로 전달
            - hasNext가 false면 마지막 페이지
            
            응답 데이터
            - 평점(avgRating): 프로그램 리뷰 평균
            - 리뷰 수(reviewCount): 작성된 리뷰 개수
            - 북마크 여부(isBookmarked): 로그인 사용자의 북마크 상태
            """
    )
    @GetMapping("/list/initial")
    public ResponseEntity<ProgramListResponse> getProgramsByUserRegion(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute ProgramInitialListRequest request
    ) {
        Long memberId = member.getId();

        ProgramListResponse response = programQueryService.getPrograms(memberId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로그램 리스트 조회 (Viewport + 필터링)",
            description = """
            지도 화면 하단 리스트용 프로그램 상세 정보 조회
            
            Viewport 좌표 (필수)
            - northEastLat, northEastLng : 상단 우측 모서리
            - southWestLat, southWestLng : 하단 좌측 모서리
            
            거리 계산 (선택)
            - userLat, userLng 제공 시: 사용자 위치 기준 거리 계산 (km)
            - 미제공 시: distance = null
            
            정렬 방식
            - 검색 조건 없음 + 사용자 위치 있음: 거리순 (가까운 순)
            - 검색 조건 있음 또는 사용자 위치 없음: 최신순 (id DESC)
            
            검색 조건
            - keyword: 프로그램명, 주소, 시설 세부유형 검색
            - city, district: 지역 필터
            - facilityTypes: 시설 유형 (다중 선택)
            - weekDayTypes: 요일 필터 (다중 선택)
            - minPrice, maxPrice: 가격 범위 (둘 다 0이면 무료만)
            - minAge, maxAge: 연령 범위
            - startDate, endDate: 날짜 범위
            
            검색 조건 조합 방식
            (Viewport 필터) AND (
                (keyword 조건) OR 
                (필터 조합: city + district + facilityTypes + price + weekday + age + date)
            )
            
            페이지네이션 (커서 기반)
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor 를 cursor 로 전달
            """
    )
    @GetMapping("/list")
    public ResponseEntity<ProgramListResponse> getProgramsByViewportAndFilters(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute ProgramListBySearchRequest request,
            @RequestParam(value = "facilityTypes", required = false) List<FacilityType> facilityTypes,
            @RequestParam(value = "weekDayTypes", required = false) List<WeekDayType> weekDayTypes
    ) {
        Long memberId = member.getId();

        ProgramListResponse response = programQueryService.getProgramsBySearch(
                memberId, request, facilityTypes, weekDayTypes
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로그램 키워드 검색",
            description = """
            프로그램명 또는 시설명으로 키워드 검색
            
            검색 방식
            - 공백 무시: "강남 체육관" = "강남체육관"
            - Prefix 검색: "수영" → "수영장", "수영교실" 등 매칭
            - 프로그램명 + 시설명 모두 검색
            
            정렬
            - ID 오름차순 (ASC)
            - 커서는 마지막 ID 이상부터 조회
            
            페이지네이션
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor를 cursor로 전달
            
            사용 예시
            - "강남 축구" → 강남 지역 축구 프로그램
            - "수영장" → 수영장 시설의 프로그램
            - "헬스" → 헬스 관련 프로그램
            """
    )
    @GetMapping("/search")
    public ResponseEntity<ProgramSimpleListResponse> searchPrograms(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute ProgramSearchByKeywordRequest request
    ) {

        Long memberId = member.getId();
        ProgramSimpleListResponse response = programQueryService.searchPrograms(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로그램 리뷰 저장")
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ProgramReviewResponse> createReview(
            @PathVariable("id") Long facilityId,
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @RequestBody SaveProgramReviewRequest request
    ) {

        Long memberId = member.getId();

        ProgramReviewResponse response = programReviewCommandService.createReview(facilityId, memberId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "프로그램 리뷰 검색",
            description = """
            프로그램 리뷰를 검색하여 조회
            
            검색 조건
            - keyword: 리뷰 제목, 프로그램명, 시설명, 리뷰 내용 검색
            - city, district: 지역 필터
            
            정렬
            - 최신순 (id DESC)
            
            페이지네이션
            - 커서 기반 (id < cursor)
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor를 cursor로 전달
            
            시설 매칭
            - 프로그램 위치 기준 100m 이내 시설 자동 매칭
            - 매칭된 시설명 우선 표시
            """
    )
    @GetMapping("/reviews")
    public ResponseEntity<ProgramReviewListResponse> getProgramReviews(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute ProgramReviewRequest request
    ) {

        Long memberId = member.getId();
        ProgramReviewListResponse result = programReviewQueryService.getProgramReviews(memberId, request);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "프로그램 상세 조회",
            description = """
            프로그램 ID로 상세 정보 조회
            
            조회 정보
            - 프로그램 기본 정보 (이름, 주소, 가격, 시간 등)
            - 리뷰 통계 (평균 평점, 리뷰 수)
            - 북마크 여부 (인증된 사용자)
            - 거리 (사용자 위치 제공 시, km 단위)
            
            사용자 위치
            - userLatitude, userLongitude 제공 시 거리 계산
            - 미제공 시 distance = null
            - 소수점 2자리까지 표시 (예: 1.23km)
            """
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProgramDetailResponse> getProgramDetail(
            @Schema(description = "프로그램 ID", example = "1")
            @PathVariable("id") Long programId,

            @Schema(description = "사용자 위도 (거리 계산용)", example = "37.5665")
            @RequestParam(value = "userLatitude", required = false) Double userLatitude,

            @Schema(description = "사용자 경도 (거리 계산용)", example = "126.9780")
            @RequestParam(value = "userLongitude", required = false) Double userLongitude,

            @AuthenticationPrincipal CustomUserDetails member
    ) {
        Long memberId = member.getId();

        ProgramDetailResponse response = programQueryService.getProgramDetail(
                programId,
                memberId,
                userLatitude,
                userLongitude
        );

        return ResponseEntity.ok(response);
    }
}
