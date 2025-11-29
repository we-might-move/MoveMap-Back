package org.wemightmove.movemap.domain.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;
import org.wemightmove.movemap.domain.member.entity.Member;

@Entity
@Getter
@Table(name = "facility_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    public static FacilityReview from(Member member, Facility facility, FacilityReviewRequest request) {
        return new FacilityReview(member, facility, request);
    }

    private FacilityReview(Member member, Facility facility, FacilityReviewRequest request) {
        this.member = member;
        this.facility = facility;
        this.rating = request.rating();
        this.title = request.title();;
        this.content = request.content();
    }
}
