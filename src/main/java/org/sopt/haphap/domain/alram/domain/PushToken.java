package org.sopt.haphap.domain.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.domain.user.entity.User;

@Getter
@Entity
@Table(
        name = "push_token",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_push_token_user_device",
                columnNames = {"user_id", "device_id"})   // 한 기기당 토큰 1개 보장
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;


    @Column(nullable = false, length = 255)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviceType deviceType;

    @Column(nullable = false)
    private boolean active;

    private PushToken(User user, String deviceId, String fcmToken, DeviceType deviceType) {
        this.user = user;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.active = true;
    }

    public static PushToken create(User user, String deviceId, String fcmToken, DeviceType deviceType) {
        return new PushToken(user, deviceId,fcmToken, deviceType);
    }

    // 같은 기기에서 토큰이 갱신됐을 때: 토큰 값을 새로 교체하고 다시 활성화
    public void renew(String fcmToken, DeviceType deviceType) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.active = true;
    }

    public void activate(DeviceType deviceType) {
        this.active = true;
        this.deviceType = deviceType;
    }

    public void deactivate() {
        this.active = false;
    }
}