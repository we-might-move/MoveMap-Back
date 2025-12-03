package org.wemightmove.movemap.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteFacilityPageResponse;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteFacilityResponse;
import org.wemightmove.movemap.domain.member.repository.MemberFacilityRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacilityQueryServiceImpl implements MemberFacilityQueryService {

    private final MemberFacilityRepository memberFacilityRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Override
    public FavoriteFacilityPageResponse getFavoriteList(Long memberId, BigDecimal currentLatitude, BigDecimal currentLongitude, Long cursor, Integer size) {

        // 1. 페이지 크기 검증 및 기본값 설정
        int validatedSize = validatePageSize(size);

        // 2. 위치 정보 검증
        List<FavoriteFacilityResponse> facilities = null;
        if (currentLatitude != null && currentLongitude != null) {
            validateLocation(currentLatitude, currentLongitude);
            // 3. size+1 개 조회 (hasNext 판단용)
            facilities = memberFacilityRepository.findFavoriteFacilityWithDistance(
                    memberId,
                    currentLatitude,
                    currentLongitude,
                    cursor,
                    validatedSize + 1
            );
        }
        else {
            facilities = memberFacilityRepository.findFavoriteFacility(memberId, cursor, validatedSize + 1);
        }

        return buildPageResponse(facilities, validatedSize);
    }

    private FavoriteFacilityPageResponse buildPageResponse(
            List<FavoriteFacilityResponse> facilities, int size
    ) {

        boolean hasNext = facilities.size() > size;

        // 실제 반환할 데이터 (size 만큼)
        List<FavoriteFacilityResponse> content = hasNext ? facilities.subList(0, size) : facilities;

        // 다음 페이지의 cursor (마지막 항목의 memberFacilityId)
        Long nextCursor = hasNext && !content.isEmpty() ?
                content.get(content.size() - 1).memberFacilityId() : null;

        return new FavoriteFacilityPageResponse(
                content,
                nextCursor,
                hasNext,
                content.size()
        );
    }

    private void validateLocation(BigDecimal latitude, BigDecimal longitude) {
//        if (latitude == null || longitude == null) {
//            throw new CustomException(ErrorCode.MISSING_PARAMETER);
//        }

        if (isOutOfRangeLatitude(latitude)) {
            throw new CustomException(ErrorCode.WRONG_LATITUDE);
        }
        if (isOutOfRangeLongitude(longitude)) {
            throw new CustomException(ErrorCode.WRONG_LONGITUDE);
        }
    }

    private int validatePageSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private boolean isOutOfRangeLatitude(BigDecimal latitude) {
        return latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0;
    }

    private boolean isOutOfRangeLongitude(BigDecimal longitude) {
        return longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0;
    }
}
