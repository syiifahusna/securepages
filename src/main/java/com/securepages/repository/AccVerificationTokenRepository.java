package com.securepages.repository;

import com.securepages.model.AccVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccVerificationTokenRepository extends JpaRepository<AccVerificationToken,Long> {

    Optional<AccVerificationToken> findByToken(String token);
}
