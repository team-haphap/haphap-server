package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.dto.request.CompanyCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.CompanyResponse;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        if (companyRepository.existsByName(request.name())) {
            throw new CustomException(PostingErrorCode.DUPLICATE_COMPANY_NAME);
        }
        Company company = companyRepository.save(Company.create(
                request.name(), request.logoImageUrl(), request.imageUrl(), request.cardLogoImageUrl()));
        return new CompanyResponse(company.getId(), company.getName(), company.getLogoImageUrl(),
                company.getImageUrl(), company.getCardLogoImageUrl());
    }
}