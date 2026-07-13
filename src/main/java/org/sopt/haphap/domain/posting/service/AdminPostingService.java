package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.request.PostingCreateRequest;
import org.sopt.haphap.domain.posting.dto.request.PostingUpdateRequest;
import org.sopt.haphap.domain.posting.dto.response.PostingAdminResponse;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPostingService {

    private final PostingRepository postingRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;

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
}