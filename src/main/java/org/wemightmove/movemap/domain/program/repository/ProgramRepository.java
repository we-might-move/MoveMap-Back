package org.wemightmove.movemap.domain.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.program.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {
}
