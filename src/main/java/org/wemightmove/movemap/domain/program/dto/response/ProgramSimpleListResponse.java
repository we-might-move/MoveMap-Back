package org.wemightmove.movemap.domain.program.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "프로그램 검색 리스트 응답")
public record ProgramSimpleListResponse(
        @Schema(description = "프로그램 목록")
        List<ProgramSimpleItem> programs,

        @Schema(description = "다음 커서 (다음 페이지 조회용)", example = "50")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "현재 페이지 아이템 개수", example = "20")
        int currentSize
) {

    public static ProgramSimpleListResponse of(
            List<ProgramSimpleItem> programs,
            Long nextCursor,
            boolean hasNext
    ) {
        return new ProgramSimpleListResponse(
                programs,
                nextCursor,
                hasNext,
                programs.size()
        );
    }

    @Schema(description = "프로그램 간단 정보")
    public record ProgramSimpleItem(
            @Schema(description = "프로그램 ID", example = "1")
            Long id,

            @Schema(description = "프로그램명", example = "청소년 축구 교실")
            String programName,

            @Schema(description = "시설명", example = "강남종합체육관")
            String facilityName,

            @Schema(description = "시설 타입", example = "축구장")
            String facilitySubtype,

            @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
            String address
    ) {
        public static ProgramSimpleItem mapToDTO(Object[] row) {
            return new ProgramSimpleItem(
                    ((Number) row[0]).longValue(),  // id
                    (String) row[1],                // program_name
                    (String) row[2],                // facility_name
                    (String) row[3],                // facility_subtype
                    (String) row[4]                 // address
            );
        }
    }
}
