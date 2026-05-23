package com.owsm.AuthService.repository;

import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
