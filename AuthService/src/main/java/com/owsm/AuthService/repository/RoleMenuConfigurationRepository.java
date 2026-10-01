package com.owsm.AuthService.repository;

import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.RoleMenuConfiguration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleMenuConfigurationRepository
    extends JpaRepository<RoleMenuConfiguration, Long> {

    Optional<RoleMenuConfiguration> findByRoleName(RoleName roleName);
}
