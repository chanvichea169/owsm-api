package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.Api.JwtUtil;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.OwsmException;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.UserService;
import com.owsm.AuthService.service.handler.UserServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserServiceHandler userServiceHandler;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;

    @Override
    public UserResponse registerUser(UserRequest request) throws OwsmException {
        userServiceHandler.validateUsername(request.getUsername());
        userServiceHandler.validateEmail(request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new OwsmException("USER_NOT_FOUND");
        }

        User user = userServiceHandler.convertToUser(request);
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId()));
            user.setRole(role);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setEnabled(false);

        userRepository.save(user);

        sendOtpEmail(user.getEmail(), otp);

        return userServiceHandler.convertToUserResponse(user);
    }

    @Override
    public UserResponse loginUser(UserRequest request) throws OwsmException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        UserResponse response = userServiceHandler.convertToUserResponse(user);
        response.setToken(token);
        return response;
    }

    @Override
    public UserResponse verifyOtp(String email, String otp) throws OwsmException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (user.getOtp() == null) {
            throw new RuntimeException("No OTP found for this user");
        }

        if (!user.getOtp().trim().equals(otp.trim())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setEnabled(true);
        user.setOtp(null);
        userRepository.save(user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String token = jwtUtil.generateToken(userDetails);
        UserResponse response = userServiceHandler.convertToUserResponse(user);
        response.setToken(token);

        return response;
    }

    @Override
    public void resendOtp(String email) throws OwsmException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (user.isEnabled()) {
            throw new RuntimeException("User is already enabled");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) throws OwsmException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        userServiceHandler.validateUsername(request.getUsername());
        userServiceHandler.validateEmail(request.getEmail());

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId()));
            user.setRole(role);
        }
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
        return userServiceHandler.convertToUserResponse(user);
    }

    @Override
    public void deleteUser(Long id) throws OwsmException {
        if (id == 1L) {
            throw new OwsmException("Credential validation failed");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));
        userRepository.delete(user);
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userServiceHandler::convertToUserResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userServiceHandler::convertToUserResponse)
                .collect(Collectors.toList());
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for registration");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }
}
