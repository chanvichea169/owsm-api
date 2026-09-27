package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    @Override
    @EntityGraph(attributePaths = "user")
    Optional<UserProfile> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "user")
    List<UserProfile> findAll();

    @EntityGraph(attributePaths = "user")
    Optional<UserProfile> findByUserId(Long userId);
}