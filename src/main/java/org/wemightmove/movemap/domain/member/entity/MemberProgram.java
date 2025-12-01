package org.wemightmove.movemap.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.program.entity.Program;

@Entity
@Getter
@Table(name = "member_program")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProgram {

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

    public static MemberProgram from(Member member, Program program) {
        return new MemberProgram(member, program);
    }

    private MemberProgram(Member member, Program program) {
        this.member = member;
        this.program = program;
    }
}
