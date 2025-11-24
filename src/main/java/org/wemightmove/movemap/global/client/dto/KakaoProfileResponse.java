package org.wemightmove.movemap.global.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoProfileResponse(
        @JsonProperty("id")
        Long id
) {
}
