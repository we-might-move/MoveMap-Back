package org.wemightmove.movemap.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record LoginResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String accessToken,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String refreshToken,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long kakaoId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Boolean isNewMember
        ) {

        public LoginResponse(String accessToken, String refreshToken) {
                this(accessToken, refreshToken, null, null);
        }

        public LoginResponse(String accessToken, String refreshToken, Boolean isNewMember) {
                this(accessToken, refreshToken, null, isNewMember);
        }

        public LoginResponse(Long kakaoId, Boolean isNewMember) {
                this(null, null, kakaoId, isNewMember);
        }
}
