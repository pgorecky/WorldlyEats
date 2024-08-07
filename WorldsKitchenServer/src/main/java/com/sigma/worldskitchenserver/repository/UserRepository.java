package com.sigma.worldskitchenserver.repository;

import com.sigma.worldskitchenserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
    default Optional<User> findByLoginOrEmail(String login) {
        Optional<User> userOptional = findByLogin(login);
        if (userOptional.isPresent()) {
            return userOptional;
        } else {
            return findByEmail(login);
        }
    }

    Optional<User> findById(Long id);
    Boolean existsByLogin(String login);
    Boolean existsByEmail(String email);
}
