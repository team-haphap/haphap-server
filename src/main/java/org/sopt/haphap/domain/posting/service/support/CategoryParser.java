package org.sopt.haphap.domain.posting.service.support;

import java.util.Arrays;
import java.util.List;

public class CategoryParser {
    public static List<String> parse(String category) {
        return parseCategories(category);
    }
    private static List<String> parseCategories(String category) {
        if (category == null || category.isBlank()) return null;
        List<String> result = Arrays.stream(category.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        return result.isEmpty() ? null : result;
    }
}