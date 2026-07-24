package org.wemightmove.movemap.global.search.index;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * {@code program_v1} 인덱스로 색인되는 프로그램 문서.
 * <p>
 * 필드는 T3 매핑({@code es/mappings/program.json})과 정확히 일치해야 하며, ES 매핑이
 * {@code dynamic: strict} 이므로 {@code @JsonProperty} 로 snake_case 필드명을 고정한다.
 * {@code source_updated_at} 은 Jackson jsr310 모듈 의존을 피하기 위해 ISO-8601 문자열로 담는다
 * (ES {@code date} 필드는 ISO-8601 문자열을 허용).
 *
 * @param id              PG primary key (ES {@code _id} 와 동일)
 * @param name            프로그램명
 * @param facilityName    시설명
 * @param facilitySubtype 시설 소분류
 * @param address         주소
 * @param contentHash     색인 텍스트 필드의 SHA-256 canonical 해시
 * @param sourceUpdatedAt 원본 row 의 {@code updated_at}(ISO-8601 instant, null 가능)
 */
public record ProgramDoc(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("facility_name") String facilityName,
        @JsonProperty("facility_subtype") String facilitySubtype,
        @JsonProperty("address") String address,
        @JsonProperty("content_hash") String contentHash,
        @JsonProperty("source_updated_at") String sourceUpdatedAt
) implements IndexedDoc {

    /**
     * content_hash 고정 순서: {@code name|facility_name|facility_subtype|address}.
     */
    public static ProgramDoc of(
            long id,
            String name,
            String facilityName,
            String facilitySubtype,
            String address,
            Instant updatedAt
    ) {
        String hash = ContentHash.of(name, facilityName, facilitySubtype, address);
        String sourceUpdatedAt = updatedAt == null ? null : updatedAt.toString();
        return new ProgramDoc(id, name, facilityName, facilitySubtype, address, hash, sourceUpdatedAt);
    }
}
