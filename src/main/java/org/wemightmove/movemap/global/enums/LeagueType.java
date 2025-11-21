package org.wemightmove.movemap.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueType {
    START("출발", "#FFB6C1"),
    WALK("걷기", "#FFD700"),
    RUN("뛰기", "#1E90FF"),
    ADVENTURE("모험", "#32CD32"),
    CHAMPION("챔피언", "#FF0000")
    ;

    private final String name;
    private final String colorCode;
}
