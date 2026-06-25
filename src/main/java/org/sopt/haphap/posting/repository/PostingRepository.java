package org.sopt.haphap.posting.repository;

import org.sopt.haphap.posting.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<Posting, Long> {
}