package org.sopt.haphap.registration.repository;

import org.sopt.haphap.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}