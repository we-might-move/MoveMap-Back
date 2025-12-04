package org.wemightmove.movemap.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record InviteInfo(
        Long parentId,
        String parentName,
        Long childId,
        String childName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime invitedAt
) {

    /**
     * TODO: 이 부분 LocalDateTime.now() 로 사용하는게 아니라 외부 LocalDateTime 유틸 만들어서 사용하기(테스트 코드 작성 시 의존성 문제 해결)
     */
    public static InviteInfo of(Long parentId, String parentName, Long childId, String childName, LocalDateTime localDateTime) {
        return new InviteInfo(parentId, parentName, childId, childName, localDateTime);
    }
}
