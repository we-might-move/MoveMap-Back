package org.wemightmove.movemap.domain.facility.dto.request;

import jakarta.validation.constraints.Size;

public record FacilityReviewRequest(
        int rating,
        @Size(min = 5)
        String title,
        @Size(min = 10)
        String content
) {
}
