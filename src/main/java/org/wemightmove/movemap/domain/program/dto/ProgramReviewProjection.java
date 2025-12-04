package org.wemightmove.movemap.domain.program.dto;

public interface ProgramReviewProjection {
    Long getReviewId();
    Long getMemberId();
    String getMemberNickname();
    Long getProgramId();
    String getProgramName();
    Integer getRating();
    String getTitle();
    String getFacilityName();
    String getFacilitySubtype();
    String getContent();
}