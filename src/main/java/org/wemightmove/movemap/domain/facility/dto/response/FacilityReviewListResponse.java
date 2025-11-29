package org.wemightmove.movemap.domain.facility.dto.response;

import java.util.List;

public record FacilityReviewListResponse(
        List<FacilityReviewItem> reviews,
        Long nextCursor,
        boolean hasNext
) {
    public static FacilityReviewListResponse of(List<FacilityReviewItem> reviews, int requestedSize) {
        boolean hasNext = reviews.size() > requestedSize;
        List<FacilityReviewItem> content = hasNext ?
                reviews.subList(0, requestedSize) : reviews;

        Long nextCursor = hasNext && !content.isEmpty() ?
                content.get(content.size() - 1).cursor() : null;

        return new FacilityReviewListResponse(content, nextCursor, hasNext);
    }
}
