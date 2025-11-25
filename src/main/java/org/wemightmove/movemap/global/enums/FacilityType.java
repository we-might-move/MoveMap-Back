package org.wemightmove.movemap.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}