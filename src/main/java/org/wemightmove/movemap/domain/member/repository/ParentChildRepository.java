package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.ParentChild;

import java.util.List;

public interface ParentChildRepository extends JpaRepository<ParentChild, Long> {
    List<ParentChild> findAllByParent(Member parent);
    List<ParentChild> findAllByChild(Member child);

}
