package org.wemightmove.movemap.domain.notification.service.scheduler;

import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;

public interface PushRetryScheduler {
    void retryFailedPushMessages();
}
