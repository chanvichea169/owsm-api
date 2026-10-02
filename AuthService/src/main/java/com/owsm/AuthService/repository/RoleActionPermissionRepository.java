package com.owsm.AuthService.repository;

import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.RoleActionPermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleActionPermissionRepository
    extends JpaRepository<RoleActionPermission, Long> {

    List<RoleActionPermission> findByRoleName(RoleName roleName);

    @Modifying
    @Query("delete from RoleActionPermission permission where permission.roleName = :roleName")
    void deleteAllByRoleName(@Param("roleName") RoleName roleName);
}
