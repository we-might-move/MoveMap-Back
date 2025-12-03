package org.wemightmove.movemap.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.facility.entity.Facility;

@Entity
@Getter
@Table(name = "member_facility")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFacility {

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

    public static MemberFacility from(Member member, Facility facility) {
        return new MemberFacility(member, facility);
    }

    private MemberFacility(Member member, Facility facility) {
        this.member = member;
        this.facility = facility;
    }
}
