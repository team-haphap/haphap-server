package org.sopt.haphap.domain.search.document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.search.engine.SearchTextNormalizer;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "posting_search")
public class PostingSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long postingId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    // 소문자 + 공백 제거. 부분 일치/띄어쓰기 변형/대소문자 무시에 사용
    @Field(type = FieldType.Keyword)
    private String companyText;

    @Field(type = FieldType.Keyword)
    private String titleText;

    @Field(type = FieldType.Long)
    private Long registeredAtEpoch;

    public static PostingSearchDocument of(Posting posting) {
        PostingSearchDocument doc = new PostingSearchDocument();
        doc.id = String.valueOf(posting.getId());
        doc.postingId = posting.getId();
        doc.categoryName = posting.getCategory().getName();
        doc.companyText = SearchTextNormalizer.normalize(posting.getCompany().getName());
        doc.titleText = SearchTextNormalizer.normalize(posting.getTitle());
        LocalDateTime created = posting.getCreatedAt();
        doc.registeredAtEpoch = created == null
                ? 0L
                : created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return doc;
    }
}