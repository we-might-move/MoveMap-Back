package org.wemightmove.movemap.domain.record.service;

import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;

public interface RecordService {
    void addSelfRecord(SelfRecordAddRequest request);
}
