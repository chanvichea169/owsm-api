package com.owsm.AuthService.securityaudit.repository;

import com.owsm.AuthService.securityaudit.entity.AuthDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthDeviceRepository extends JpaRepository<AuthDevice, UUID> {
    Page<AuthDevice> findByUser_Id(Long userId, Pageable pageable);
    List<AuthDevice> findByUser_IdAndRevokedAtIsNull(Long userId);
    Optional<AuthDevice> findByUser_IdAndDeviceId(Long userId, UUID deviceId);
    long countByUser_IdAndRevokedAtIsNull(Long userId);
}
