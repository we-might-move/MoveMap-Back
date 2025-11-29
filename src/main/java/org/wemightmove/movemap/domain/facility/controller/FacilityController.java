package org.wemightmove.movemap.domain.facility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityInitialListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilitySearchListRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
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
            @Valid @ModelAttribute(value = "searchConditions", name = "searchConditions") FacilityMarkerRequest request,
            @RequestParam(value = "facilityTypes", required = false) List<FacilityType> facilityTypes
            ) {
        FacilityMarkerResponse response = facilityQueryService.searchMarkers(member.getId(), request, facilityTypes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/initial")
    @Operation(
            summary = "초기 시설 리스트 조회",
            description = "앱 진입 시 사용자 위치 기반으로 시설 리스트를 조회합니다. " +
                    "토큰에서 추출한 회원의 지역 중심으로 반경 5km 내 시설을 거리순으로 정렬하여 반환합니다."
    )
    public ResponseEntity<FacilityListResponse> getInitialFacilityList(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size" , required = false) Integer size
    ) {
        Long memberId = member.getId();
        FacilityInitialListRequest request = new FacilityInitialListRequest(cursor, size);

        FacilityListResponse response = facilityQueryService.getFacilityList(memberId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(
            summary = "뷰포트 기반 시설 리스트 조회",
            description = "지도 뷰포트 영역 내 시설을 조회합니다. " +
                    "검색 조건(키워드, 지역, 타입, 바우처)을 적용할 수 있으며, " +
                    "검색 조건이 있으면 최신순, 없으면 거리순으로 정렬합니다."
    )
    public ResponseEntity<FacilityListResponse> getFacilityListByViewport(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @ModelAttribute(value = "searchConditions") FacilitySearchListRequest request,
            @RequestParam(value = "facilityTypes", required = false) List<FacilityType> facilityTypes
    ) {
        Long memberId = member.getId();

        FacilityListResponse response = facilityQueryService.searchFacilityList(memberId, request, facilityTypes);
        return ResponseEntity.ok(response);
    }

    /**
     * FIXME : 강남 축구 -> 이런 식으로 검색해도 잘 나오도록 개선하기
     */
    @GetMapping("/facilities/search")
    @Operation(
            summary = "시설 검색",
            description = "시설 검색만 진행합니다. 시설 리뷰를 쓸 때 시설을 찾는 용도로 사용됩니다."
    )
    public ResponseEntity<FacilitySimpleListResponse> searchFacilityListByKeyword(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long memberId = member.getId();

        FacilitySimpleListResponse response = facilityQueryService.searchFacilityListByKeyword(memberId, keyword);

        return ResponseEntity.ok(response);
    }
}
