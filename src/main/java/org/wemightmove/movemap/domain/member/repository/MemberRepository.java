package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByKakaoId(Long kakaoId);
    Optional<Member> findByUuid(String uuid);

    // 테스트용 메서드
    long countByRole(String role);

    @Query("SELECT m FROM Member m WHERE m.role = :role ORDER BY m.id")
    List<Member> findTop10ByRole(String role);

    @Query("SELECT m FROM Member m WHERE m.role = :role ORDER BY m.id")
    List<Member> findTop100ByRole(String role);
}
