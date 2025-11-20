package org.wemightmove.movemap.global.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.wemightmove.movemap.global.client.dto.KakaoProfileResponse;
import org.wemightmove.movemap.global.client.dto.KakaoTokenResponse;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final RestClient restClient = RestClient.builder()
            .defaultStatusHandler(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (request, response) -> {
                        throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
                    })
            .build();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;


    // 인가 코드로 토큰 요청
    public KakaoTokenResponse getAccessToken(String code) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "authorization_code");
            form.add("client_id", clientId);
            form.add("redirect_uri", redirectUri);
            form.add("code", code);
            return restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }


    // 액세스 토큰으로 사용자 정보 요청
    public KakaoProfileResponse getUserInfo(String accessToken) {
        try {
            return restClient.post()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoProfileResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return factory;
    }
}
