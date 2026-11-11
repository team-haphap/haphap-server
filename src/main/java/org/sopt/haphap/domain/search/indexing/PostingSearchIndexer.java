package org.sopt.haphap.domain.search.indexing;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostingSearchIndexer {

    private final ElasticsearchOperations operations;
    private final PostingRepository postingRepository;

    /* 공고 1건 색인(없으면 색인에서 제거) */
    @Transactional(readOnly = true)
    public void index(Long postingId) {
        postingRepository.findWithCompanyAndCategory(postingId).ifPresentOrElse(
                posting -> operations.save(PostingSearchDocument.of(posting)),
                () -> operations.delete(String.valueOf(postingId), PostingSearchDocument.class));
    }

    /* 전체 재색인: 인덱스를 지우고 DB 기준으로 다시 만듦 (삭제된 공고 잔재 제거) */
    @Transactional(readOnly = true)
    public int reindexAll() {
        IndexOperations indexOps = operations.indexOps(PostingSearchDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();

        List<PostingSearchDocument> docs = postingRepository.findAllWithCompanyAndCategory(null).stream()
                .map(PostingSearchDocument::of)
                .toList();
        if (!docs.isEmpty()) {
            operations.save(docs);
        }
        return docs.size();
    }
}