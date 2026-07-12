package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.dto.request.CategoryCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.CategoryResponse;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new CustomException(PostingErrorCode.DUPLICATE_CATEGORY_NAME);
        }
        Category category = categoryRepository.save(Category.create(request.name(), request.cardImageUrl()));
        return new CategoryResponse(category.getId(), category.getName(), category.getCardImageUrl());
    }
}
