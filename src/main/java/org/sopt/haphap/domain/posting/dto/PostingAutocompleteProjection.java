package org.sopt.haphap.domain.posting.dto;

public interface PostingAutocompleteProjection {
    Long getId();
    String getTitle();
    String getLogoImageUrl();
}

//엔티티 전체를 가져오지 않고, 필요한 애들만 가져오기 위해서 인터페이스 프로젝션으로 받도록 구현했습니다
