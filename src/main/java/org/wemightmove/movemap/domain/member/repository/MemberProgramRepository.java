package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.MemberProgram;

public interface MemberProgramRepository extends JpaRepository<MemberProgram, Long>, MemberProgramRepositoryCustom {
}
