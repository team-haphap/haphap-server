package org.sopt.haphap.domain.posting.service.support;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.CategoryErrorCode;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryParser {

    private final CategoryRepository categoryRepository;

    public List<String> parse(List<String> category) {
        if (category == null || category.isEmpty()) {
            return null;
        }

        List<String> categories = category.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (categories.isEmpty()) {
            return null;
        }

        validate(categories);

        return categories;
    }

    private void validate(List<String> categories) {
        Set<String> existingNames = categoryRepository.findByNameIn(categories)
                .stream()
                .map(category -> category.getName())
                .collect(Collectors.toSet());

        boolean hasInvalidCategory = categories.stream()
                .anyMatch(category -> !existingNames.contains(category));

        if (hasInvalidCategory) {
            throw new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }
}