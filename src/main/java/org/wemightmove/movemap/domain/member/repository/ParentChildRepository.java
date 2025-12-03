package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.ParentChild;

import java.util.List;

public interface ParentChildRepository extends JpaRepository<ParentChild, Long> {
    List<ParentChild> findAllByParent(Member parent);

    @Query("""
       SELECT pc.child 
       FROM ParentChild pc
       JOIN  pc.child 
       WHERE pc.parent = :parent
       """)
    List<Member> findChildMembersByParent(@Param("parent") Member parent);
    List<ParentChild> findAllByChild(Member child);

    boolean existsByParentAndChild(Member parent, Member child);

    @Modifying
    @Query("DELETE FROM ParentChild pc WHERE pc.parent.id = :memberId OR pc.child.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);
}
