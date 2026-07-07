package org.sopt.haphap.domain.posting.dto;

import java.util.List;

public record ParticipantSummary(
        long registeredCount,
        List<String> profileImages
) {}