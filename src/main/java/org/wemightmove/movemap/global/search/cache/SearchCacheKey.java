package org.wemightmove.movemap.global.search.cache;

import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;

import java.util.Locale;

/**
 * 검색 캐시 키 빌더(설계 §4.2).
 * <ul>
 *   <li>program: {@code mm:search:v{ver}:program:{engine}:{normKw}:{cursor}:{size}}</li>
 *   <li>facility: {@code mm:search:v{ver}:facility:{engine}:{normKw}}</li>
 * </ul>
 * {@code normKw} = 소문자화 + trim. program 은 추가로 {@code normalizedKeyword()}(공백 제거)를 적용한다
 * ("YOGA"/"yoga", "수영 "/"수영" 이 같은 키를 공유 → 히트율↑). {@code cursor} 가 null 이면 리터럴 {@code none}.
 */
public final class SearchCacheKey {

    private static final String PREFIX = "mm:search:v";
    private static final String NO_CURSOR = "none";

    private SearchCacheKey() {
    }

    /** program 검색 캐시 키. */
    public static String program(int version, String engine, ProgramSearchByKeywordRequest request) {
        String normKw = normalize(request.normalizedKeyword());
        String cursor = request.cursor() == null ? NO_CURSOR : String.valueOf(request.cursor());
        return PREFIX + version + ":program:" + engine + ':' + normKw + ':' + cursor + ':' + request.size();
    }

    /** facility 검색 캐시 키(size=30 고정·커서 없음). */
    public static String facility(int version, String engine, String keyword) {
        String normKw = normalize(keyword == null ? "" : keyword);
        return PREFIX + version + ":facility:" + engine + ':' + normKw;
    }

    private static String normalize(String keyword) {
        return keyword.toLowerCase(Locale.ROOT).trim();
    }
}
