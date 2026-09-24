package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.request.PostingCreateRequest;
import org.sopt.haphap.domain.posting.dto.request.PostingUpdateRequest;
import org.sopt.haphap.domain.posting.dto.response.PostingAdminResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingCurrentStageResponse;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPostingService {

    private final PostingRepository postingRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PostingStageRepository postingStageRepository;

    @Transactional
    public PostingAdminResponse createPosting(PostingCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(PostingErrorCode.CATEGORY_NOT_FOUND));
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new CustomException(PostingErrorCode.COMPANY_NOT_FOUND));

        Posting posting = postingRepository.save(Posting.create(
                request.title(), request.deadline(), request.location(), request.position(), category, company));
        return PostingAdminResponse.from(posting);
    }

    @Transactional
    public PostingAdminResponse updatePosting(Long postingId, PostingUpdateRequest request) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(PostingErrorCode.CATEGORY_NOT_FOUND));
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new CustomException(PostingErrorCode.COMPANY_NOT_FOUND));

        posting.update(request.title(), request.deadline(), request.location(), request.position(), category, company);
        return PostingAdminResponse.from(posting);
    }

    // 운영진 수동 전형 이동 (전형 이동은 자동 이동 또는 운영진 수동 이동을 통해서만 변경).
    // 자동 이동과 달리 orderIndex 인접성을 강제하지 않는다 — 건너뛰기 승인, 잘못 이동된 상태 교정 모두 이 경로로 처리.
    @Transactional
    public PostingCurrentStageResponse moveCurrentStage(Long postingId, Long stageId) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));
        PostingStage stage = postingStageRepository.findById(stageId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.STAGE_NOT_FOUND));
        if (!stage.belongsTo(posting)) {
            throw new CustomException(PostingErrorCode.STAGE_NOT_IN_POSTING);
        }

        stage.markMoved(LocalDateTime.now());
        posting.moveCurrentStageTo(stage);
        return PostingCurrentStageResponse.from(posting);
    }
}