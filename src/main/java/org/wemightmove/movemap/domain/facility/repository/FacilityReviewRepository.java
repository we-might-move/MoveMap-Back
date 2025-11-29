package org.wemightmove.movemap.domain.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.entity.FacilityReview;
import org.wemightmove.movemap.domain.member.entity.Member;

public interface FacilityReviewRepository extends JpaRepository<FacilityReview, Long> {
    boolean existsFacilityReviewByMemberAndFacility(Member member, Facility facility);
}
