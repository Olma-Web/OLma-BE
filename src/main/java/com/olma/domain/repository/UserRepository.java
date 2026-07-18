package com.olma.domain.repository;

import com.olma.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.tokenVersion FROM User u WHERE u.id= :userId")
    Optional<Integer> findTokenVersionById(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion+1 WHERE u.id = :userId")
    void increaseTokenVersion(@Param("userId") Long userId);
}
