package org.wemightmove.movemap.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record LoginResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String accessToken,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long kakaoId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Boolean isNewMember
        ) {

        public LoginResponse(String accessToken) {
                this(accessToken, null, null);
        }

        public LoginResponse(String accessToken, Boolean isNewMember) {
                this(accessToken, null, isNewMember);
        }

        public LoginResponse(Long kakaoId, Boolean isNewMember) {
                this(null, kakaoId, isNewMember);
        }
}
