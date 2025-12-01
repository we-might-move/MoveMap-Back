package org.wemightmove.movemap.global.enums;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public enum WeekDayType {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int num;

    public static List<Integer> getWeekDayRange(List<WeekDayType> weekDayTypeList) {
        return weekDayTypeList.stream().map(w -> w.num).toList();
    }
}
