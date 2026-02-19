package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.RoleRequest;
import com.owsm.AuthService.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@CrossOrigin
public class RoleController {
    private final RoleService roleService;
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody RoleRequest roleRequest) {
        try {
            return ResponseEntity.ok(roleService.createRole(roleRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }
}
