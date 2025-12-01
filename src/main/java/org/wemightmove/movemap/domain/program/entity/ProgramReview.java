package org.wemightmove.movemap.domain.program.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.member.entity.Member;

@Entity
@Getter
@Table(name = "program_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgramReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Builder
    public ProgramReview(Member member, Program program, int rating, String title, String content) {
        this.member = member;
        this.program = program;
        this.rating = rating;
        this.title = title;
        this.content = content;
    }
}
