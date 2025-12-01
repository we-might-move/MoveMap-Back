package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Member> findByKakaoId(Long kakaoId);
    boolean existsByKakaoId(Long kakaoId);
    Optional<Member> findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    @Query("SELECT COUNT(m) > 0 FROM Member m " +
            "WHERE m.nickname = :nickname " +
            "AND m.id != :excludeMemberId " +
            "AND m.isDeleted = false")
    boolean existsByNicknameExcludingMember(
            @Param("nickname") String nickname,
            @Param("excludeMemberId") Long excludeMemberId
    );

    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Member> findActiveById(@Param("id") Long id);
}
