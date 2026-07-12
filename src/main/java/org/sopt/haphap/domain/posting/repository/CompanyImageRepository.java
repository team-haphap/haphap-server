package org.sopt.haphap.domain.posting.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyImageRepository extends JpaRepository<CompanyImage, Long> {
    Optional<CompanyImage> findByCompanyIdAndType(Long companyId, CompanyImageType type);

    @Query("""
        SELECT ci FROM CompanyImage ci
        WHERE ci.company.id IN :companyIds AND ci.type = :type
        """)
    List<CompanyImage> findByCompanyIdInAndType(
            @Param("companyIds") List<Long> companyIds,
            @Param("type") CompanyImageType type);
}