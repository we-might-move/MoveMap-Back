package org.wemightmove.movemap.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

public record MemberScoreResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer score,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Double percent
) {
        @Builder
        public MemberScoreResponse(Integer score, Double percent) {
                this.score = score;
                this.percent = percent;
        }
}
