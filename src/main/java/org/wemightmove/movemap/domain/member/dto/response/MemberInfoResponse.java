package org.wemightmove.movemap.domain.member.dto.response;

import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.enums.SexType;

public record MemberInfoResponse(
        String email,
        RoleType role,
        String nickname,
        String school,
        String city,
        String district,
        SexType sex,
        Integer age,
        Double height,
        Double weight
) {

    public static MemberInfoResponse from(Member member, String city, String district) {
        return new MemberInfoResponse(
                member.getEmail(),
                member.getRole(),
                member.getNickname(),
                member.getSchool(),
                city, district,
                member.getSex(),
                member.getAge(),
                member.getHeight(),
                member.getWeight()
        );
    }
}
