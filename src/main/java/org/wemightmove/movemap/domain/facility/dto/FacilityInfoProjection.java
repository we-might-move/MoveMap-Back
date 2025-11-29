package org.wemightmove.movemap.domain.facility.dto;

import java.math.BigDecimal;

public interface FacilityInfoProjection {
    Long getId();
    String getName();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getFacilityType();
    String getFacilitySubtype();
    String getAddress();
    Boolean getIsVoucherAvailable();
    Double getDistanceMeters();
    Double getAvgRating();
    Long getReviewCount();
    Boolean getIsBookmarked();
}