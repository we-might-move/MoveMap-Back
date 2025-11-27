package org.wemightmove.movemap.domain.member.dto.response;

import java.time.LocalDateTime;

public record MemberWithdrawResponse(
        String message,
        LocalDateTime withdrawnAt,
        WithdrawStatistics statistics
) {

    public record WithdrawStatistics(
            int removedParentRelations,
            int removedChildRelations,
            int removedFavorites,
            int removedPrograms,
            int removedNotifications,
            int totalRemovedRelations
    ) { }
}
