package com.securepages.repository;

import com.securepages.model.ResetPasswordToken;
import com.securepages.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken,Long> {

    boolean existsByTokenAndDateUpdatedIsNotNull(String token);
    Optional<ResetPasswordToken> findByToken(String token);
}
