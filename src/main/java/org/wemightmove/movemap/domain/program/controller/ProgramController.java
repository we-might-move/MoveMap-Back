package org.wemightmove.movemap.domain.program.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.program.dto.request.*;
import org.wemightmove.movemap.domain.program.dto.response.ProgramListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.service.ProgramQueryService;
import org.wemightmove.movemap.domain.program.service.ProgramReviewCommandService;
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

    @Operation(summary = "프로그램 마커 조회(초기)")
    @GetMapping("/markers/initial")
    public ResponseEntity<ProgramMarkerResponse> getInitialMarkers(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        ProgramMarkerResponse response = programQueryService.getMarkers(member.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "뷰포트 프로그램 마커 조회 (검색 + 필터)",
            description = """
                    지도 영역(viewport) 내 프로그램을 필터링하여 마커로 반환합니다.
                    
                    필터 조건:
                    - 키워드: 프로그램명 검색 (부분 일치)
                    - 지역: 시/도 + 시/군/구
                    - 시설 타입: BALL_GAME, MARTIAL_ARTS, FITNESS 등
                    - 가격: 최소/최대 금액
                    - 요일: 월 ~ 일
                    - 연령: 최소/최대 나이
                    
                    정렬:
                    - 필터 있음: 프로그램 수 많은 순
                    - 필터 없음: 거리순 (viewport 중심 기준)
                    
                    성능:
                    - Bounding Box 기반 공간 인덱스 활용
                    - 응답 시간: ~20ms
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

    /**
     * 1. 프로그램 리스트 조회 (초기) - 사용자 지역 기반
     * GET /api/v1/programs/list?cursor=100&size=20
     */
    @GetMapping("/list/initial")
    @Operation(summary = "프로그램 리스트 조회 (초기)",
            description = "사용자가 등록한 지역 기반으로 프로그램 목록을 조회합니다. 커서 기반 페이징을 사용합니다.")
    public ResponseEntity<ProgramListResponse> getProgramsByUserRegion(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute ProgramInitialListRequest request
    ) {
        Long memberId = member.getId();

        ProgramListResponse response = programQueryService.getPrograms(memberId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 프로그램 리스트 조회 (뷰포트 + 필터링)
     * GET /api/v1/programs/search?northEastLat=37.6&northEastLng=127.1&...
     */
    @GetMapping("/list")
    @Operation(summary = "프로그램 리스트 조회 (필터링)",
            description = "뷰포트 및 다양한 조건으로 프로그램 목록을 조회합니다. 커서 기반 페이징을 사용합니다.")
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
            프로그램명 또는 시설명으로 키워드 검색합니다.
            - 공백을 무시하고 검색: "강남 체육관" = "강남체육관"
            - 프로그램명과 시설명 모두에서 검색
            - 커서 기반 페이지네이션 지원
            
            예시:
            - "강남 축구" → 강남 지역의 축구 프로그램 검색
            - "수영장" → 수영장이 있는 시설의 프로그램 검색
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

    @Operation(summary = "프로그램 리뷰를 저장합니다.")
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
}
