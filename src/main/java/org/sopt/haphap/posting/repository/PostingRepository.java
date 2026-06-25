package org.sopt.haphap.posting.repository;

import org.sopt.haphap.posting.entity.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<Posting, Long> {
}