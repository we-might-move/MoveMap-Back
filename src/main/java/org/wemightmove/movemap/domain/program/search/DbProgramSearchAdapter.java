package org.wemightmove.movemap.domain.program.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;

import java.util.List;

/**
 * 프로그램 키워드 검색 DB 어댑터.
 * <p>
 * 기존 {@code ProgramQueryServiceImpl#searchPrograms} 에 있던 native 쿼리 경로
 * ({@code programRepository.searchProgramsByKeyword} + size+1 probe 페이지네이션)를
 * 바이트 동일하게 옮겨온 것이다. 커서 계약(id 오름차순, {@code id > cursor})·hasNext·nextCursor·
 * currentSize 계산은 원본과 동일하다.
 */
@Component
@RequiredArgsConstructor
public class DbProgramSearchAdapter {

    private final ProgramRepository programRepository;

    public ProgramSimpleListResponse search(ProgramSearchByKeywordRequest request) {
        String normalizedKeyword = request.normalizedKeyword();
        int size = request.size();

        // size + 1개를 조회하여 hasNext 판단
        List<ProgramSimpleListResponse.ProgramSimpleItem> results =
                programRepository.searchProgramsByKeyword(
                        normalizedKeyword,
                        request.cursor(),
                        size + 1
                );

        // hasNext 판단 및 응답 생성
        boolean hasNext = results.size() > size;
        List<ProgramSimpleListResponse.ProgramSimpleItem> content = hasNext ?
                results.subList(0, size) : results;
        Long nextCursor = hasNext ? content.get(content.size() - 1).id() : null;

        return ProgramSimpleListResponse.of(content, nextCursor, hasNext);
    }
}
