package com.logicgames.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<User,Long>{

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);
    Optional<User> findByVerificationCode(String verificationCode);
    // --- ¡AÑADE ESTOS MÉTODOS! ---
    Optional<User> findByResetCode(String resetCode);
    Optional<User> findByResetToken(String token); //
}
