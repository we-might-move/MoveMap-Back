package org.wemightmove.movemap.global.search.index;

/**
 * ES 로 색인되는 도메인 문서의 공통 계약.
 * <p>
 * bulk 색인·리컨실리에이션(content_hash 비교)에서 도메인(program/facility)에 무관하게
 * 문서를 다루기 위한 최소 인터페이스다. 각 구현 record 는 T3 매핑과 <b>정확히</b> 일치하는
 * 필드를 {@code @JsonProperty} 로 직렬화한다(매핑이 {@code dynamic: strict} 이므로 필드명이
 * 하나라도 어긋나면 색인이 거부된다).
 */
public interface IndexedDoc {

    /** PG primary key. ES {@code _id} 로도 사용된다. */
    long id();

    /** 색인 텍스트 필드의 SHA-256 canonical 해시(드리프트 비교용). */
    String contentHash();
}
