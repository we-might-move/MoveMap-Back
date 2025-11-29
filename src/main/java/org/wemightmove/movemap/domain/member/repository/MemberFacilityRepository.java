package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberFacility;

public interface MemberFacilityRepository extends JpaRepository<MemberFacility, Long>, MemberFacilityCustomRepository {

    @Modifying
    @Query("DELETE FROM MemberFacility mf WHERE mf.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);

    boolean existsMemberFacilitiesByMemberAndFacility(Member member, Facility facility);

    void deleteByMemberAndFacility(Member member, Facility facility);
}
