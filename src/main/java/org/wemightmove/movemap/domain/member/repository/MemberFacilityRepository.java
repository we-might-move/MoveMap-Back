package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.MemberFacility;

public interface MemberFacilityRepository extends JpaRepository<MemberFacility, Long>, MemberFacilityCustomRepository {
}
