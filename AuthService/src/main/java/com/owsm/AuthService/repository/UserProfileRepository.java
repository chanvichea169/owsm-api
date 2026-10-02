package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Removes the profile row before the owning user is deleted. The profile
     * foreign key has no cascade rule, so it must be cleared explicitly.
     */
    @Modifying
    @Query("delete from UserProfile profile where profile.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}