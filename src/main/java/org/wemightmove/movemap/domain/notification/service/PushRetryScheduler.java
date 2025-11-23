package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;

public interface PushRetryScheduler {
    void retryFailedPushMessages();
}
