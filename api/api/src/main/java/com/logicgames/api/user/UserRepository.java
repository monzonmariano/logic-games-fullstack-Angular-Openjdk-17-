package com.logicgames.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserRepository  extends JpaRepository<User,Long>{

    Optional<User> findByEmail(String email);
    // Spring Data JPA creará la consulta SQL por nosotros
    Optional<User> findByResetToken(String resetToken);
}
