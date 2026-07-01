package org.sopt.haphap.domain.alram.dispatch;

import org.sopt.haphap.domain.alram.notification.NotificationMessage;
import java.util.List;

public record AlramDispatch(
        NotificationMessage message, List<SendTarget> targets
) {
    public static AlramDispatch empty() {
        return new AlramDispatch(null, List.of());
    }
    public boolean isEmpty() {
        return targets.isEmpty();
    }
}