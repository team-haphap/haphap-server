package org.sopt.haphap.domain.posting.dto;

import java.util.List;

public record ParticipantSummary(
        long registeredCount,
        List<Participant> participants
) {
    public record Participant(Long userId, String profileImageUrl) {}
}