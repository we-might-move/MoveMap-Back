package org.wemightmove.movemap.domain.program.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ProgramDetailProjection {

    Long getId();
    String getName();
    String getFacilityType();
    String getFacilitySubtype();

    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getAddress();
    String getHmpgUrl();

    LocalDate getBeginDate();
    LocalDate getEndDate();

    Integer getWeekdayNumber();
    Integer getPrice();

    LocalTime getStartTime();
    LocalTime getEndTime();

    Integer getTarget();
    Integer getCapacity();

    BigDecimal getDistance();
    BigDecimal getAvgRating();
    Integer getReviewCount();
    Boolean getIsBookmarked();
}
