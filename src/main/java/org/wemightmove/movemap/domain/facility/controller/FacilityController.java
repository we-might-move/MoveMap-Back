package org.wemightmove.movemap.domain.facility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.domain.facility.service.FacilityCommandService;
import org.wemightmove.movemap.domain.facility.service.FacilityQueryService;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.util.List;

@RestController
@Tag(name = "Facility")
@RequiredArgsConstructor
@RequestMapping("/facilities")
public class FacilityController {
    private final FacilityCommandService facilityCommandService;
    private final FacilityQueryService facilityQueryService;

    @PostMapping("/{id}/bookmarks")
    public ResponseEntity<Void> bookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.addBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> deleteBookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.deleteBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 마커 조회(초기)")
    @GetMapping("/markers/initial")
    public ResponseEntity<FacilityMarkerResponse> getInitialMarkers(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        FacilityMarkerResponse response = facilityQueryService.getMarkers(member.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "시설 마커 조회 (Viewport + 검색)",
            description = """
                화면 영역(Viewport) 내 시설 마커 조회
                - 검색 조건 있음: 키워드, 필터 적용 + 최신순 정렬
                - 검색 조건 없음: 거리순 정렬 (Viewport 중심 기준)
                - 최대 500개 반환 (maxResults 로 조정 가능)
                
                검색 조건:
                - keyword: 시설명 검색
                - city: 시/도 입력
                - district : 시/군/구 입력
                - facilityTypes: 시설 유형 (BALL_GAME, FITNESS 등)
                - isVoucherAvailable: 스포츠강좌이용권 사용 가능 여부
                """
    )
    @GetMapping("/markers")
    public ResponseEntity<FacilityMarkerResponse> searchMarkers(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute("searchConditions") FacilityMarkerRequest request,
            @RequestParam(value = "facilityTypes", required = false) List<FacilityType> facilityTypes
            ) {
        FacilityMarkerResponse response = facilityQueryService.searchMarkers(member.getId(), request, facilityTypes);
        return ResponseEntity.ok(response);
    }
}
