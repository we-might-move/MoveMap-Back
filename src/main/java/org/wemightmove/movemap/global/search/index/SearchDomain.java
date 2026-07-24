package org.wemightmove.movemap.global.search.index;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * 검색 색인 대상 도메인(program/facility)의 단일 출처(single source of truth).
 * <p>
 * 인덱스/alias 이름, 원본 테이블, native SELECT 컬럼 순서, row→{@link IndexedDoc} 매핑을 한곳에 모아
 * {@link BulkReindexer}(전량 색인)와 {@code ReconciliationJob}(델타/드리프트)이 공유한다. 새 필드가
 * 추가되면 여기와 대응 {@code *Doc}·매핑 JSON 만 고치면 된다.
 */
public enum SearchDomain {

    PROGRAM(
            "program",
            "program_v1",
            "program_search",
            "program",
            "id, name, facility_name, facility_subtype, address, updated_at",
            ProgramDoc.class
    ) {
        @Override
        public MappedRow mapRow(Object[] row) {
            long id = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String facilityName = (String) row[2];
            String facilitySubtype = (String) row[3];
            String address = (String) row[4];
            Instant updatedAt = toInstant(row[5]);
            return new MappedRow(ProgramDoc.of(id, name, facilityName, facilitySubtype, address, updatedAt), updatedAt);
        }
    },

    FACILITY(
            "facility",
            "facility_v1",
            "facility_search",
            "facility",
            "id, name, facility_type, facility_subtype, address, updated_at",
            FacilityDoc.class
    ) {
        @Override
        public MappedRow mapRow(Object[] row) {
            long id = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String facilityType = (String) row[2];
            String facilitySubtype = (String) row[3];
            String address = (String) row[4];
            Instant updatedAt = toInstant(row[5]);
            return new MappedRow(FacilityDoc.of(id, name, facilityType, facilitySubtype, address, updatedAt), updatedAt);
        }
    };

    private final String label;
    private final String indexName;
    private final String aliasName;
    private final String tableName;
    private final String selectColumns;
    private final Class<? extends IndexedDoc> docType;

    SearchDomain(
            String label,
            String indexName,
            String aliasName,
            String tableName,
            String selectColumns,
            Class<? extends IndexedDoc> docType
    ) {
        this.label = label;
        this.indexName = indexName;
        this.aliasName = aliasName;
        this.tableName = tableName;
        this.selectColumns = selectColumns;
        this.docType = docType;
    }

    /** 메트릭 태그({@code domain=program|facility})와 요약 응답 키로 쓰는 라벨. */
    public String label() {
        return label;
    }

    /** 색인 대상 concrete 인덱스명(예: {@code program_v1}). bulk/refresh/mget 은 이 이름을 쓴다. */
    public String indexName() {
        return indexName;
    }

    /** 검색/카운트에 쓰는 alias(예: {@code program_search}). */
    public String aliasName() {
        return aliasName;
    }

    public String tableName() {
        return tableName;
    }

    public Class<? extends IndexedDoc> docType() {
        return docType;
    }

    /** 전량 색인용 keyset SQL: {@code id > :last} 오름차순 페이지. */
    public String fullSelectSql() {
        return "SELECT " + selectColumns + " FROM " + tableName
                + " WHERE id > :last ORDER BY id ASC LIMIT :limit";
    }

    /** 델타 리컨실용 keyset SQL: {@code updated_at > :since AND id > :last} 오름차순 페이지. */
    public String deltaSelectSql() {
        return "SELECT " + selectColumns + " FROM " + tableName
                + " WHERE updated_at > :since AND id > :last ORDER BY id ASC LIMIT :limit";
    }

    /** row(Object[]) → 색인 문서 + 원본 updated_at(워터마크 계산용). */
    public abstract MappedRow mapRow(Object[] row);

    /**
     * timestamptz 컬럼이 JDBC/Hibernate 를 거쳐 돌아오는 타입(Timestamp/OffsetDateTime 등)을 {@link Instant} 로 정규화.
     */
    private static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof Date date) { // java.sql.Timestamp 는 java.util.Date 하위
            return date.toInstant();
        }
        throw new IllegalStateException("지원하지 않는 updated_at 타입: " + value.getClass().getName());
    }

    /**
     * row 매핑 결과: 색인 문서와 원본 {@code updated_at}(델타 워터마크 전진용).
     *
     * @param doc       ES 색인 문서
     * @param updatedAt 원본 row 의 updated_at(워터마크 계산용, null 가능)
     */
    public record MappedRow(IndexedDoc doc, Instant updatedAt) {
    }
}
