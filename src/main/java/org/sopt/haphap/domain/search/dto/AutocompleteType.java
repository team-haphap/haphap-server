package org.sopt.haphap.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AutocompleteType {
    COMPANY("company"),
    KEYWORD("keyword");

    private final String value;

    AutocompleteType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}