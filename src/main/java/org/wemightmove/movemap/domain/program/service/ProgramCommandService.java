package org.wemightmove.movemap.domain.program.service;

public interface ProgramCommandService {
    void addBookmarkProgram(Long memberId, Long programId);
    void deleteBookmarkProgram(Long memberId, Long programId);
}
