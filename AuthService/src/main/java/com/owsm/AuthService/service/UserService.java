package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.OwsmException;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponse registerUser(UserRequest request) throws OwsmException;
    UserResponse registerAdminUser(UserRequest request) throws OwsmException;
    UserResponse registerDepartmentUser(UserRequest request, Long departmentId) throws OwsmException;
    UserResponse loginUser(UserRequest request) throws OwsmException;
    UserResponse verifyOtp(String email, String otp) throws OwsmException;
    void resendOtp(String email) throws OwsmException;
    UserResponse updateUser(Long id, UserRequest request) throws OwsmException;
    UserResponse updateAdminUser(Long id, UserRequest request) throws OwsmException;
    UserResponse updateDepartmentUser(Long id, UserRequest request, Long departmentId) throws OwsmException;
    Optional<UserResponse> getUserById(Long id);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByDepartmentId(Long departmentId);
    void deleteUser(Long id) throws OwsmException;

    void changePassword(Long id, String currentPassword, String newPassword) throws OwsmException;
    void setUserEnabled(Long id, boolean enabled) throws OwsmException;
    boolean toggleUserEnabled(Long id) throws OwsmException;

    UserResponse updateUserLocation(Long id, String streetAddress, String villageCode) throws OwsmException;

    UserResponse getUserWithLocation(Long id) throws OwsmException;

    List<UserResponse> getUsersByVillage(String villageCode);

    List<UserResponse> getUsersByCommune(Integer communeCode);

    List<UserResponse> getUsersByDistrict(Integer districtCode);

    List<UserResponse> getUsersByProvince(Integer provinceCode);
}