package org.sopt.haphap.domain.user.repository;

import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    @Query("SELECT u.anonymousName FROM User u WHERE u.anonymousName IN :names")
    List<String> findAnonymousNamesIn(@Param("names") List<String> names);
}