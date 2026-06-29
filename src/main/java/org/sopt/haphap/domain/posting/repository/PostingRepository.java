package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<Posting, Long> {
}