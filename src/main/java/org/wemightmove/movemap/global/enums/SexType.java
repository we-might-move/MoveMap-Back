package org.wemightmove.movemap.global.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SexType {
    WOMAN("여자"),
    MAN("남자"),
    OTHER("선택 안함");

    private final String name;
}
