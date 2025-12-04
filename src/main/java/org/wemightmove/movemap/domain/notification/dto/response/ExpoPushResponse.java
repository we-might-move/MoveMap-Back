// ExpoPushResponse.java
package org.wemightmove.movemap.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExpoPushResponse(
        @JsonProperty("data")
        List<ExpoPushTicket> data
) {
    public record ExpoPushTicket(
            @JsonProperty("status")
            String status,

            @JsonProperty("id")
            String id,

            @JsonProperty("message")
            String message,

            @JsonProperty("details")
            ExpoPushError details
    ) {
        public boolean isSuccess() {
            return "ok".equals(status);
        }

        public boolean isDeviceNotRegistered() {
            return details != null && "DeviceNotRegistered".equals(details.error());
        }

        public boolean isRetryable() {
            if (details == null) return false;
            String error = details.error();
            return "MessageRateExceeded".equals(error) ||
                    "ServiceUnavailable".equals(error);
        }
    }

    public record ExpoPushError(
            @JsonProperty("error")
            String error
    ) {}
}