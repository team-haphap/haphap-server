package org.sopt.haphap.domain.search.engine;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.search.code.SearchErrorCode;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchEngine {

    private static final int MAX_HITS = 2000;
    private static final String FIELD_SEARCH_TEXT = "searchText";
    private static final String FIELD_CATEGORY = "categoryName";

    private final ElasticsearchOperations operations;
    private final SearchTermExpander termExpander;

    public PostingSearchHitResult search(String keyword, List<String> categories) {
        List<List<String>> tokenGroups = termExpander.expandTokens(keyword);
        if (tokenGroups.isEmpty()) {
            return PostingSearchHitResult.empty();
        }
        try {
            List<PostingSearchHit> all = run(buildQuery(tokenGroups, categories, true));
            if (tokenGroups.size() == 1) {
                return new PostingSearchHitResult(all, List.of());
            }
            Set<Long> allIds = new HashSet<>();
            all.forEach(h -> allIds.add(h.postingId()));
            List<PostingSearchHit> partial = run(buildQuery(tokenGroups, categories, false)).stream()
                    .filter(h -> !allIds.contains(h.postingId()))
                    .toList();
            return new PostingSearchHitResult(all, partial);
        } catch (DataAccessException e) {
            log.error("[검색] Elasticsearch 조회 실패 keyword={}", keyword, e);
            throw new CustomException(SearchErrorCode.SEARCH_ENGINE_UNAVAILABLE);
        }
    }

    private List<PostingSearchHit> run(Query query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withMaxResults(MAX_HITS)
                .build();
        SearchHits<PostingSearchDocument> hits = operations.search(nativeQuery, PostingSearchDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> new PostingSearchHit(doc.getPostingId(), doc.getRegisteredAtEpoch()))
                .toList();
    }

    private Query buildQuery(List<List<String>> tokenGroups, List<String> categories, boolean requireAll) {
        List<Query> tokenQueries = tokenGroups.stream().map(this::anyOf).toList();
        return Query.of(q -> q.bool(b -> {
            if (requireAll) {
                b.must(tokenQueries);
            } else {
                b.should(tokenQueries).minimumShouldMatch("1");
            }
            if (categories != null && !categories.isEmpty()) {
                b.filter(categoryFilter(categories));
            }
            return b;
        }));
    }

    /* 한 단어의 대체어(동의어 포함) 중 하나라도 부분 일치 */
    private Query anyOf(List<String> alternatives) {
        List<Query> wildcards = alternatives.stream()
                .map(alt -> Query.of(q -> q.wildcard(w -> w
                        .field(FIELD_SEARCH_TEXT)
                        .value("*" + escape(alt) + "*"))))
                .toList();
        return Query.of(q -> q.bool(b -> b.should(wildcards).minimumShouldMatch("1")));
    }

    private Query categoryFilter(List<String> categories) {
        List<FieldValue> values = categories.stream().map(FieldValue::of).toList();
        return Query.of(q -> q.terms(t -> t.field(FIELD_CATEGORY).terms(v -> v.value(values))));
    }

    // 사용자 입력의 와일드카드 문자 무력화
    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }
}