package org.wemightmove.movemap.domain.member.dto.response;

import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.Role;
import org.wemightmove.movemap.global.enums.SexType;

public record MemberInfoResponse(
        String email,
        Role role,
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
