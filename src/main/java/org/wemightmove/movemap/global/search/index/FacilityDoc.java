package org.wemightmove.movemap.global.search.index;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * {@code facility_v1} 인덱스로 색인되는 시설 문서.
 * <p>
 * 필드는 T3 매핑({@code es/mappings/facility.json})과 정확히 일치해야 하며, ES 매핑이
 * {@code dynamic: strict} 이므로 {@code @JsonProperty} 로 snake_case 필드명을 고정한다.
 *
 * @param id              PG primary key (ES {@code _id} 와 동일)
 * @param name            시설명
 * @param facilityType    시설 대분류(enum STRING, 예: {@code BALL_GAME})
 * @param facilitySubtype 시설 소분류
 * @param address         주소
 * @param contentHash     색인 텍스트 필드의 SHA-256 canonical 해시
 * @param sourceUpdatedAt 원본 row 의 {@code updated_at}(ISO-8601 instant, null 가능)
 */
public record FacilityDoc(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("facility_type") String facilityType,
        @JsonProperty("facility_subtype") String facilitySubtype,
        @JsonProperty("address") String address,
        @JsonProperty("content_hash") String contentHash,
        @JsonProperty("source_updated_at") String sourceUpdatedAt
) implements IndexedDoc {

    /**
     * content_hash 고정 순서: {@code name|facility_type|facility_subtype|address}.
     */
    public static FacilityDoc of(
            long id,
            String name,
            String facilityType,
            String facilitySubtype,
            String address,
            Instant updatedAt
    ) {
        String hash = ContentHash.of(name, facilityType, facilitySubtype, address);
        String sourceUpdatedAt = updatedAt == null ? null : updatedAt.toString();
        return new FacilityDoc(id, name, facilityType, facilitySubtype, address, hash, sourceUpdatedAt);
    }
}
