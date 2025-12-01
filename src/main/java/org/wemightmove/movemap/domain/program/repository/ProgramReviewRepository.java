package org.wemightmove.movemap.domain.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.program.entity.ProgramReview;

public interface ProgramReviewRepository extends JpaRepository<ProgramReview, Long> {
    /**
     * 특정 회원이 특정 프로그램에 작성한 리뷰 존재 여부 확인
     */
    boolean existsByMemberIdAndProgramId(Long memberId, Long programId);
}
