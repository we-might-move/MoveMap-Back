package org.wemightmove.movemap.domain.program.search;

import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;

/**
 * 프로그램 키워드 검색 포트(엔진 추상화).
 * <p>
 * 기존 {@code ProgramQueryService#searchPrograms} 의 검색 계약을 그대로 미러링한다:
 * 입력은 {@link ProgramSearchByKeywordRequest}, 출력은 {@link ProgramSimpleListResponse}.
 * 구현체는 ES({@link EsProgramSearchAdapter}) 와 DB({@link DbProgramSearchAdapter}) 두 가지이며,
 * feature flag({@code movemap.search.program.engine}) 와 fallback 은 서비스 레이어(라우터)가 담당한다.
 * <p>
 * GLOBAL CONSTRAINT §2 — ES 문서 타입은 이 포트 밖으로 새어나가지 않는다(어댑터에서 기존 DTO 로 매핑).
 */
public interface ProgramSearchPort {

    ProgramSimpleListResponse search(ProgramSearchByKeywordRequest request);
}
