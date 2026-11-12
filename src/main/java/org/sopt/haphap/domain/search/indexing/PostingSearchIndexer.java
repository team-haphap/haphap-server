package org.sopt.haphap.domain.search.indexing;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchIndexer {

    private final ElasticsearchOperations operations;
    private final PostingRepository postingRepository;

    /* 공고 1건 색인(없으면 색인에서 제거) */
    @Transactional(readOnly = true)
    public void index(Long postingId) {
        postingRepository.findWithCompanyAndCategory(postingId).ifPresentOrElse(
                posting -> saveIgnoringStaleWrite(PostingSearchDocument.of(posting)),
                () -> operations.delete(String.valueOf(postingId), PostingSearchDocument.class));
    }

    private void saveIgnoringStaleWrite(PostingSearchDocument doc) {
        try {
            operations.save(doc);
        } catch (DataAccessException e) {
            if (isVersionConflict(e)) {
                log.debug("[검색] 더 최신 문서가 이미 색인되어 있어 건너뜀 postingId={}", doc.getPostingId());
                return;
            }
            throw e;
        }
    }

    private boolean isVersionConflict(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains("version_conflict_engine_exception")) {
                return true;
            }
        }
        return false;
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