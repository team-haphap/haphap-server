package org.sopt.haphap.domain.posting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

@Entity
@Getter
@Table(name = "stage_result_count",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stage_result_count",
                columnNames = {"posting_id", "stage_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageResultCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "posting_id", nullable = false)
    private Long postingId;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @Column(nullable = false)
    private long passCount;

    @Column(nullable = false)
    private long failCount;

    @Column(nullable = false)
    private long pendingCount;

    private StageResultCount(Long postingId, Long stageId,
                             long passCount, long failCount, long pendingCount) {
        this.postingId = postingId;
        this.stageId = stageId;
        this.passCount = passCount;
        this.failCount = failCount;
        this.pendingCount = pendingCount;
    }

    // 신규 등록 시 최초 row 생성 (해당 result만 1)
    public static StageResultCount init(Long postingId, Long stageId, RegistrationResult result) {
        long pass = result == RegistrationResult.PASS ? 1 : 0;
        long fail = result == RegistrationResult.FAIL ? 1 : 0;
        long pending = result == RegistrationResult.PENDING ? 1 : 0;
        return new StageResultCount(postingId, stageId, pass, fail, pending);
    }

    public long total() {
        return passCount + failCount + pendingCount;
    }

    public static StageResultCount empty(Long postingId, Long stageId) {
        return new StageResultCount(postingId, stageId, 0, 0, 0);
    }

    public void add(RegistrationResult result, long n) {
        switch (result) {
            case PASS -> this.passCount += n;
            case FAIL -> this.failCount += n;
            case PENDING -> this.pendingCount += n;
        }
    }
}