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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserServiceHandler userServiceHandler;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;

    // ================= REGISTER =================

    @Override
    public UserResponse registerUser(UserRequest request) throws OwsmException {

        userServiceHandler.validateUsername(request.getUsername());
        userServiceHandler.validateEmail(request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new OwsmException("EMAIL_ALREADY_EXISTS");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new OwsmException("USERNAME_ALREADY_EXISTS");
        }

        User user = userServiceHandler.convertToUser(request);

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new OwsmException("ROLE_NOT_FOUND"));
            user.setRole(role);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOtp(generateOtp());
        user.setEnabled(false);

        userRepository.save(user);

        sendOtpEmail(user.getEmail(), user.getOtp());

        return userServiceHandler.convertToUserResponse(user);
    }

    // ================= LOGIN =================

    @Override
    public UserResponse loginUser(UserRequest request) throws OwsmException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new OwsmException("ACCOUNT_NOT_VERIFIED");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String token = jwtUtil.generateToken(userDetails);

        UserResponse response =
                userServiceHandler.convertToUserResponse(user);
        response.setToken(token);

        return response;
    }

    // ================= VERIFY OTP =================

    @Override
    public UserResponse verifyOtp(String email, String otp)
            throws OwsmException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (user.getOtp() == null) {
            throw new OwsmException("OTP_EXPIRED");
        }

        if (!user.getOtp().trim().equals(otp.trim())) {
            throw new OwsmException("INVALID_OTP");
        }

        user.setEnabled(true);
        user.setOtp(null);

        userRepository.save(user);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String token = jwtUtil.generateToken(userDetails);

        UserResponse response =
                userServiceHandler.convertToUserResponse(user);
        response.setToken(token);

        return response;
    }

    // ================= RESEND OTP =================

    @Override
    public void resendOtp(String email) throws OwsmException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (user.isEnabled()) {
            throw new OwsmException("ACCOUNT_ALREADY_VERIFIED");
        }

        user.setOtp(generateOtp());
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), user.getOtp());
    }

    // ================= UPDATE USER =================

    @Override
    public UserResponse updateUser(Long id, UserRequest request)
            throws OwsmException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        userServiceHandler.validateUsername(request.getUsername());
        userServiceHandler.validateEmail(request.getEmail());

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new OwsmException("EMAIL_ALREADY_EXISTS");
        }

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new OwsmException("USERNAME_ALREADY_EXISTS");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new OwsmException("ROLE_NOT_FOUND"));
            user.setRole(role);
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return userServiceHandler.convertToUserResponse(user);
    }

    // ================= DELETE =================

    @Override
    public void deleteUser(Long id) throws OwsmException {

        if (id == 1L) {
            throw new OwsmException("SUPER_ADMIN_CANNOT_BE_DELETED");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        userRepository.delete(user);
    }

    // ================= GET =================

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

    // ================= UTILITIES =================

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("OWSM Account Verification OTP");
        message.setText("Your OTP code is: " + otp);
        mailSender.send(message);
    }
}