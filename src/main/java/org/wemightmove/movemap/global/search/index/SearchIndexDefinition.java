package org.wemightmove.movemap.global.search.index;

/**
 * ES 인덱스 부트스트랩 대상 하나(도메인)를 표현하는 값 객체.
 * <p>
 * GLOBAL CONSTRAINT §7(매핑 규칙)·§10(패키지 관례)에 따라 도메인 무관 인프라 코드는
 * {@code global/search/index/}에 위치한다. 실제 인덱스/alias 이름과 매핑 리소스 경로는
 * {@link IndexBootstrapper}가 이 레코드의 목록을 순회하며 사용한다.
 *
 * @param indexName    실제 인덱스 이름 (예: {@code program_v1})
 * @param aliasName    검색에서 참조할 alias 이름 (예: {@code program_search})
 * @param mappingPath  classpath 상의 매핑/세팅 JSON 리소스 경로 (예: {@code es/mappings/program.json})
 */
public record SearchIndexDefinition(String indexName, String aliasName, String mappingPath) {
}
