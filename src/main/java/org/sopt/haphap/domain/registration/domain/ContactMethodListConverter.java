package org.sopt.haphap.domain.registration.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ContactMethodListConverter implements AttributeConverter<List<ContactMethod>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<ContactMethod> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;   // 빈 리스트/null은 null로 저장 (PENDING 케이스)
        }
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<ContactMethod> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();   // null이면 빈 리스트로
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .map(ContactMethod::valueOf)
                .collect(Collectors.toList());
    }
}