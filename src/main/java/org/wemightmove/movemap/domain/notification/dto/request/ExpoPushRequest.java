package org.wemightmove.movemap.domain.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ExpoPushRequest(
        @JsonProperty("to")
        List<String> to,

        @JsonProperty("title")
        String title,

        @JsonProperty("body")
        String body,

        @JsonProperty("data")
        Map<String, String> data,

        @JsonProperty("sound")
        String sound,

        @JsonProperty("priority")
        String priority,

        @JsonProperty("ttl")
        Integer ttl,

        @JsonProperty("badge")
        Integer badge,

        @JsonProperty("channelId")
        String channelId
) {
    public static ExpoPushRequest of(
            String expoPushToken,
            String title,
            String body,
            Map<String, String> data
    ) {
        return new ExpoPushRequest(
                List.of(expoPushToken),
                title,
                body,
                data,
                "default",
                "high",
                3600,
                1,
                "default"
        );
    }
}