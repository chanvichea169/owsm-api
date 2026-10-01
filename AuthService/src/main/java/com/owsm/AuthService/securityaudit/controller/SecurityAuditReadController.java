package com.owsm.AuthService.securityaudit.controller;

import com.owsm.AuthService.securityaudit.dto.AuditSearchRequest;
import com.owsm.AuthService.securityaudit.dto.LoginAuditResponse;
import com.owsm.AuthService.securityaudit.dto.SecurityEventResponse;
import com.owsm.AuthService.securityaudit.service.AuditReadAuthorization;
import com.owsm.AuthService.securityaudit.service.SecurityAuditReadService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class SecurityAuditReadController {
    private final SecurityAuditReadService readService;
    private final AuditReadAuthorization authorization;

    public SecurityAuditReadController(
            SecurityAuditReadService readService,
            AuditReadAuthorization authorization
    ) {
        this.readService = readService;
        this.authorization = authorization;
    }

    @GetMapping("/login-audits")
    public Page<LoginAuditResponse> loginAudits(@ModelAttribute AuditSearchRequest filters) {
        authorization.requireAuditReader();
        return readService.loginAudits(
                filters.getUserId(), filters.getUsername(), filters.getEmail(), filters.getIpAddress(),
                filters.getStatus(), filters.getDateFrom(), filters.getDateTo(), filters.getSuspicious(),
                filters.getDevice(), filters.getApplication(), filters.getPage(), filters.getSize(),
                filters.getSort(), filters.getDirection()
        );
    }

    @GetMapping("/login-audits/{id}")
    public LoginAuditResponse loginAudit(@PathVariable Long id) {
        authorization.requireAuditReader();
        return readService.loginAudit(id);
    }

    @GetMapping("/users/{userId}/login-history")
    public Page<LoginAuditResponse> userLoginHistory(
            @PathVariable Long userId,
            @ModelAttribute AuditSearchRequest filters
    ) {
        authorization.requireAuditReader();
        return readService.userLoginHistory(
                userId, filters.getUsername(), filters.getEmail(), filters.getIpAddress(),
                filters.getStatus(), filters.getDateFrom(), filters.getDateTo(), filters.getSuspicious(),
                filters.getDevice(), filters.getApplication(), filters.getPage(), filters.getSize(),
                filters.getSort(), filters.getDirection()
        );
    }

    @GetMapping("/security-events")
    public Page<SecurityEventResponse> securityEvents(@ModelAttribute AuditSearchRequest filters) {
        authorization.requireAuditReader();
        return readService.securityEvents(
                filters.getUserId(), filters.getUsername(), filters.getEmail(), filters.getIpAddress(),
                filters.getStatus(), filters.getDateFrom(), filters.getDateTo(), filters.getSuspicious(),
                filters.getDevice(), filters.getApplication(), filters.getPage(), filters.getSize(),
                filters.getSort(), filters.getDirection()
        );
    }

    @GetMapping("/security-events/{id}")
    public SecurityEventResponse securityEvent(@PathVariable Long id) {
        authorization.requireAuditReader();
        return readService.securityEvent(id);
    }
}
