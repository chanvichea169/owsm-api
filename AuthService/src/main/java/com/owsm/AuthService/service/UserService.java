package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.OwsmException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    UserResponse registerUser(UserRequest request) throws OwsmException;
    UserResponse loginUser(UserRequest request) throws OwsmException;
    UserResponse verifyOtp(String email, String otp) throws OwsmException;
    void resendOtp(String email) throws OwsmException;
    UserResponse updateUser(Long id, UserRequest request) throws OwsmException;
    void deleteUser(Long id) throws OwsmException;
    Optional<UserResponse> getUserById(Long id);
    List<UserResponse> getAllUsers();
}
