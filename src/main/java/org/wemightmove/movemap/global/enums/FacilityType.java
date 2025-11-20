package org.wemightmove.movemap.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum FacilityType {
    BALL_GAME("구기"),
    MARTIAL_ARTS("무도·격투기"),
    FITNESS("피트니스"),
    DANCE("무용·댄스"),
    AQUATIC("수상·빙상"),
    LEISURE("레저·야외"),
    COMPLEX("종합체육시설"),
    ETC("기타");

    private final String name;

    @JsonCreator
    public static FacilityType from(String s) {
        try {
            return FacilityType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }
    }
}