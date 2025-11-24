package org.wemightmove.movemap.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

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

    @JsonCreator
    public static LeagueType from(String s) {
        try {
            return LeagueType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }
    }
}
