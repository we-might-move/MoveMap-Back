package org.wemightmove.movemap.domain.facility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.facility.dto.request.*;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityReviewListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
import org.wemightmove.movemap.domain.facility.service.FacilityCommandService;
import org.wemightmove.movemap.domain.facility.service.FacilityQueryService;
import org.wemightmove.movemap.domain.facility.service.FacilityReviewService;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Validated
@Tag(name = "Facility")
@RequiredArgsConstructor
@RequestMapping("/facilities")
public class FacilityController {
    private final FacilityCommandService facilityCommandService;
    private final FacilityQueryService facilityQueryService;
    private final FacilityReviewService facilityReviewService;

    @Operation(summary = "시설 북마크 추가")
    @PostMapping("/{id}/bookmarks")
    public ResponseEntity<Void> bookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.addBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 북마크 삭제")
    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> deleteBookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.deleteBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 마커 조회(초기)", description = "사용자 등록 지역구 반경 1km 시설 조회")
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
                - northEastLat, northEastLng : 상단 우측 모서리
                - southWestLat, southWestLng : 하단 우측 모서리
                - 검색 조건 있음: 키워드, 필터 적용 + 최신순 정렬
                - 검색 조건 없음: 거리순 정렬 (Viewport 중심 기준)
                - 최대 500개 반환 (maxResults 로 조정 가능)
                
                검색 조건:
                - keyword: 시설명 검색
                - city: 시/도 입력
                - district : 시/군/구 입력
                - facilityTypes: 시설 유형 (BALL_GAME, FITNESS 등)
                - isVoucherAvailable: 스포츠강좌이용권 사용 가능 여부
                
                검색 조건 조합 방식:
                (Viewport 필터) AND
                (
                    (keyword 조건) OR (city + district + facilityTypes + voucher 필터)
                )
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
    @Operation(summary = "초기 시설 리스트 조회", description = "사용자 등록 지역구 반경 1km 시설 조회")
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
            summary = "시설 리스트 조회 (Viewport + 검색 + 페이지네이션)",
            description = """
            지도 화면 하단 리스트용 시설 상세 정보 조회
            
            Viewport 좌표 (필수)
            - northEastLat, northEastLng : 상단 우측 모서리
            - southWestLat, southWestLng : 하단 좌측 모서리
            
            정렬 방식
            - 검색 조건 없음: 거리순 (Viewport 중심 기준) + distance 필드 포함
            - 검색 조건 있음: 최신순 (id DESC) + distance 필드 null
            
            검색 조건
            - keyword: 시설명 검색
            - city: 시/도
            - district: 시/군/구
            - facilityTypes: 시설 유형 (다중 선택 가능)
            - isVoucherAvailable: 스포츠강좌이용권 사용 가능 여부
            
            검색 조건 조합 방식
            (Viewport 필터) AND (
                (keyword 조건) OR (city + district + facilityTypes + voucher 필터)
            )
            
            페이지네이션 (커서 기반)
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor 값을 cursor 파라미터로 전달
            - hasNext가 false면 마지막 페이지
            """
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

    @GetMapping("/{id}")
    @Operation(summary = "시설 상세 조회")
    public ResponseEntity<FacilityListResponse.FacilityInfo> getFacilityDetail(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable("id") Long facilityId,
            @RequestParam("lat")
            @Parameter(description = "사용자 위도", example = "37.5219")
            BigDecimal latitude,
            @RequestParam("lng")
            @Parameter(description = "사용자 경도", example = "127.1230")
            BigDecimal longitude
    ) {
        Long memberId = member.getId();

        FacilityListResponse.FacilityInfo response = facilityQueryService.getFacilityInfo(memberId, facilityId, latitude, longitude);

        return ResponseEntity.ok(response);
    }

    /**
     * FIXME : 강남 축구 -> 이런 식으로 검색해도 잘 나오도록 개선하기
     */
    @GetMapping("/search")
    @Operation(
            summary = "시설 검색",
            description = "시설 검색만 진행합니다. 시설 리뷰를 쓸 때 시설을 찾는 용도로 사용됩니다."
    )
    public ResponseEntity<FacilitySimpleListResponse> searchFacilityListByKeyword(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "keyword")
            @NotBlank(message = "검색어를 입력해주세요")
            @Size(max = 50, message = "검색어는 50자 이하여야 합니다")
            String keyword
    ) {
        Long memberId = member.getId();

        FacilitySimpleListResponse response = facilityQueryService.searchFacilityListByKeyword(memberId, keyword);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 리뷰 추가")
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Void> registerFacilityReview(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable("id") Long facilityId, @Valid @RequestBody FacilityReviewRequest request) {

        Long memberId = member.getId();

        facilityReviewService.saveFacilityReview(memberId, facilityId, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "위치 기반 리뷰 조회 (거리순 정렬)",
            description = """
            사용자 위치 기준으로 가까운 시설의 리뷰 조회
            
            정렬 방식
            - 거리순 (사용자 위치 기준) → 가까운 시설의 리뷰부터 표시
            - 같은 거리면 최신 리뷰 우선 (id DESC)
            
            검색 조건
            - keyword: 시설명 또는 주소 검색 (부분 일치)
            - city: 시/도 필터
            - district: 시/군/구 필터
            
            페이지네이션 (커서 기반)
            - 첫 페이지: cursor 없이 요청
            - 다음 페이지: 응답의 nextCursor 값을 cursor 로 전달
            """
    )
    @GetMapping("/reviews")
    public ResponseEntity<FacilityReviewListResponse> getFacilityReviewList(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "lat", required = false) BigDecimal latitude,
            @RequestParam(value = "lng", required = false) BigDecimal longitude,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {

        Long memberId = member.getId();

        FacilityReviewListRequest request = new FacilityReviewListRequest(
                latitude,
                longitude,
                city,
                district,
                keyword,
                cursor,
                size
        );

        FacilityReviewListResponse response = facilityReviewService.getReviewList(memberId, request);

        return ResponseEntity.ok(response);
    }
}
