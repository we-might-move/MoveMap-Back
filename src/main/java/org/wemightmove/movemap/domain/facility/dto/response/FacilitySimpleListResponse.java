package org.wemightmove.movemap.domain.facility.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.wemightmove.movemap.domain.facility.entity.Facility;

import java.util.List;

public record FacilitySimpleListResponse(
        List<FacilitySimpleInfo> facilities
) {
    @Schema(description = "시설 간단 정보")
    public record FacilitySimpleInfo(
            @Schema(description = "시설 ID", example = "1")
            Long id,

            @Schema(description = "시설명", example = "올림픽공원 테니스장")
            String name,

            @Schema(description = "시설 타입", example = "BALL_GAME")
            String facilityType,

            @Schema(description = "시설 서브타입", example = "테니스")
            String facilitySubtype,

            @Schema(description = "주소", example = "서울특별시 송파구 올림픽로 424")
            String address
    ) {
        public static FacilitySimpleInfo of(Facility facility) {
            return new FacilitySimpleInfo(
                    facility.getId(), facility.getName(), facility.getFacilityType().getName(), facility.getFacilitySubtype(), facility.getAddress()
            );
        }
    }
}
