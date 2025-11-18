package com.logicgames.api.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository  extends JpaRepository<User,Long>{

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    // --- ¡AÑADE ESTOS MÉTODOS! ---

    Optional<User> findByResetToken(String token); //

    @Modifying
    @Transactional
    void deleteByIsVerifiedFalseAndCreatedAtBefore(LocalDateTime cutOffDate);
}
