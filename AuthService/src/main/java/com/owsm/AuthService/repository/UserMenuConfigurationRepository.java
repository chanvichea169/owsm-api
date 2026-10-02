package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.UserMenuConfiguration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMenuConfigurationRepository
    extends JpaRepository<UserMenuConfiguration, Long> {

    Optional<UserMenuConfiguration> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Modifying
    @Query("delete from UserMenuConfiguration configuration where configuration.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
