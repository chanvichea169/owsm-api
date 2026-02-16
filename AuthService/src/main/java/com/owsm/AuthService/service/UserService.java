package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.HmsException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponse registerUser(UserRequest request) throws HmsException;
    UserResponse loginUser(UserRequest request) throws HmsException;
    UserResponse verifyOtp(String email, String otp) throws HmsException;
    void resendOtp(String email) throws HmsException;
    UserResponse updateUser(Long id, UserRequest request) throws HmsException;
    void deleteUser(Long id) throws HmsException;
    Optional<UserResponse> getUserById(Long id);
    List<UserResponse> getAllUsers();
}
