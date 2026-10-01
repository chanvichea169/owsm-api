package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.UserMenuConfiguration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMenuConfigurationRepository
    extends JpaRepository<UserMenuConfiguration, Long> {

    Optional<UserMenuConfiguration> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
