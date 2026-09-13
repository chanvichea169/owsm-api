package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.api.JwtUtil;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.OwsmException;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.UserService;
import com.owsm.AuthService.service.handler.UserServiceHandler;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final long OTP_RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final UserServiceHandler userServiceHandler;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;

    // ================= REGISTER =================

    @Override
    public UserResponse registerUser(UserRequest request) throws OwsmException {

        userServiceHandler.validateUsername(request.getUsername());
        userServiceHandler.validateEmail(request.getEmail());

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new OwsmException("EMAIL_ALREADY_EXISTS");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new OwsmException("USERNAME_ALREADY_EXISTS");
        }

        User user = userServiceHandler.convertToUser(request);
        user.setEmail(email);

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                    .orElseThrow(() -> new OwsmException("ROLE_NOT_FOUND"));
            user.setRole(role);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOtp(generateOtp());
        user.setOtpCreatedAt(LocalDateTime.now());
        user.setEnabled(false);
        user.setActive(true);

        userRepository.save(user);

        // Send OTP at registration — failures are logged but not rethrown
        sendOtpEmail(user.getEmail(), user.getOtp());

        return userServiceHandler.convertToUserResponse(user);
    }

    // ================= LOGIN =================

    @Override
    public UserResponse loginUser(UserRequest request) throws OwsmException {
        String rawIdentifier = extractLoginIdentifier(request);

        // Emails are case-insensitive; usernames are not
        String loginIdentifier = rawIdentifier.contains("@")
                ? rawIdentifier.toLowerCase()
                : rawIdentifier;

        User user = userRepository.findByEmail(loginIdentifier)
                .orElseGet(() ->
                        userRepository.findByUsername(loginIdentifier).orElse(null));

        if (user == null) {
            throw new OwsmException("USER_NOT_FOUND");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new OwsmException("INVALID_CREDENTIALS");
        }

        if (!user.isActive()) {
            throw new OwsmException("ACCOUNT_DISABLED");
        }

        // Always send a fresh OTP — never issue a token directly from login
        user.setOtp(generateOtp());
        user.setOtpCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), user.getOtp());

        UserResponse response = userServiceHandler.convertToUserResponse(user);
        response.setToken(null);
        return response;
    }

    // ================= VERIFY OTP =================

    @Override
    public UserResponse verifyOtp(String email, String otp) throws OwsmException {

        String normalized = email == null ? "" : email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (!user.isActive()) {
            throw new OwsmException("ACCOUNT_DISABLED");
        }

        if (user.getOtp() == null || user.getOtp().isBlank()) {
            throw new OwsmException("OTP_EXPIRED");
        }

        if (!user.getOtp().trim().equals(otp.trim())) {
            throw new OwsmException("INVALID_OTP");
        }

        // Mark verified and clear OTP state
        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpCreatedAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        UserResponse response = userServiceHandler.convertToUserResponse(user);
        response.setToken(token);
        return response;
    }

    // ================= RESEND OTP =================

    @Override
    public void resendOtp(String email) throws OwsmException {

        String normalized = email == null ? "" : email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (!user.isActive()) {
            throw new OwsmException("ACCOUNT_DISABLED");
        }

        // Cooldown: prevent spam
        if (user.getOtpCreatedAt() != null
                && user.getOtpCreatedAt()
                .plusSeconds(OTP_RESEND_COOLDOWN_SECONDS)
                .isAfter(LocalDateTime.now())) {
            throw new OwsmException("OTP_RESEND_TOO_SOON");
        }

        user.setOtp(generateOtp());
        user.setOtpCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), user.getOtp());
    }

    // ================= UPDATE USER =================

    @Override
    public UserResponse updateUser(Long id, UserRequest request) throws OwsmException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!user.getUsername().equals(request.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                throw new OwsmException("USERNAME_ALREADY_EXISTS");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (!user.getEmail().equals(email)
                    && userRepository.existsByEmail(email)) {
                throw new OwsmException("EMAIL_ALREADY_EXISTS");
            }
            user.setEmail(email);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
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
        return userRepository.findAll().stream()
                .map(userServiceHandler::convertToUserResponse)
                .collect(Collectors.toList());
    }

    // ================= CHANGE PASSWORD =================

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword)
            throws OwsmException {

        if (newPassword == null || newPassword.length() < 6) {
            throw new OwsmException("PASSWORD_TOO_SHORT");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new OwsmException("CURRENT_PASSWORD_INCORRECT");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new OwsmException("NEW_PASSWORD_MUST_DIFFER");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ================= ENABLE / DISABLE =================

    @Override
    public void setUserEnabled(Long id, boolean enabled) throws OwsmException {

        if (id == 1L && !enabled) {
            throw new OwsmException("SUPER_ADMIN_CANNOT_BE_DISABLED");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (!enabled && isCurrentUser(user.getUsername())) {
            throw new OwsmException("CANNOT_DISABLE_YOURSELF");
        }

        user.setActive(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public boolean toggleUserEnabled(Long id) throws OwsmException {

        if (id == 1L) {
            throw new OwsmException("SUPER_ADMIN_CANNOT_BE_DISABLED");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new OwsmException("USER_NOT_FOUND"));

        if (user.isActive() && isCurrentUser(user.getUsername())) {
            throw new OwsmException("CANNOT_DISABLE_YOURSELF");
        }

        boolean newState = !user.isActive();
        user.setActive(newState);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return newState;
    }

    // ================= UTILITIES =================

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    /**
     * Send a styled HTML OTP email.
     *
     * Uses MimeMessage + MimeMessageHelper so the client picks HTML when
     * supported, otherwise falls back to plain text.
     *
     * Failures are logged but NOT rethrown — the surrounding transaction
     * (user creation, OTP persist) must still commit so the user can retry
     * via /resend-otp.
     */
    private void sendOtpEmail(String to, String otp) {
        log.info("Attempting to send OTP to {} (code: {})", to, otp);
        try {
            // multipart = true so we can attach both plain text and HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            // Include the code in the subject so it's visible in the inbox preview
            helper.setSubject("OWSM · Your Verification Code (" + otp + ")");
            // Plain text first (fallback), HTML second (preferred)
            helper.setText(buildOtpPlainText(otp), buildOtpHtml(otp));

            mailSender.send(message);
            log.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", to, e.getMessage(), e);
            // Swallow — user record and OTP remain saved.
        }
    }

    /**
     * Plain-text fallback for email clients that don't render HTML.
     */
    private String buildOtpPlainText(String otp) {
        return """
            One Window Service Mechanism
            ----------------------------------------
            Your verification code is: %s

            This code is valid for 5 minutes.
            If you did not request this code, please ignore this email.

            — OWSM Team
            """.formatted(otp);
    }

    /**
     * Styled HTML body with the code in a prominent card.
     * Table-based layout + inline CSS → renders correctly in Gmail,
     * Outlook, Apple Mail, and most webmail clients.
     */
    /**
     * Styled HTML body with the code in a prominent card.
     * Table-based layout + inline CSS → renders correctly in Gmail,
     * Outlook, Apple Mail, and most webmail clients.
     *
     * Font: "Khmer OS Siemreab" first, with fallbacks for clients that
     * don't have it installed (Android, iOS, macOS).
     *
     * NOTE: %% escapes literal % inside String.formatted()
     */
    private String buildOtpHtml(String otp) {
        // Space digits for readability: 130529 → "1 3 0 5 2 9"
        String spacedOtp = String.join(" ", otp.split(""));

        return """
        <!DOCTYPE html>
        <html lang="km">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>លេខកូដផ្ទៀងផ្ទាត់ OWSM</title>
          <style>
            /* Khmer OS Siemreab first, then platform fallbacks */
            body, table, td, div, p, h1, strong, span {
              font-family: 'Khmer OS Siemreab', 'Khmer OS', 'Siemreab',
                           'Kantumruy Pro', 'Noto Sans Khmer',
                           'Segoe UI', Roboto, Arial, sans-serif;
            }
          </style>
        </head>
        <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer','Segoe UI',Roboto,Arial,sans-serif;color:#0f172a;line-height:1.8;">
          <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="background-color:#f1f5f9;padding:32px 12px;">
            <tr>
              <td align="center">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width:520px;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,0.08);">

                  <!-- Header -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e3a8a 100%%);padding:28px 24px;text-align:center;">
                      <div style="display:inline-block;background-color:rgba(255,255,255,0.12);border:1px solid rgba(255,255,255,0.25);border-radius:999px;padding:6px 14px;color:#a5f3fc;font-size:11px;font-weight:700;letter-spacing:2px;text-transform:uppercase;font-family:'Segoe UI',Roboto,Arial,sans-serif;">
                        OWSM
                      </div>
                      <h1 style="margin:14px 0 0;color:#ffffff;font-size:22px;font-weight:700;letter-spacing:0;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer',Arial,sans-serif;">
                        យន្តការច្រកចេញចូលតែមួយ
                      </h1>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:32px 28px 8px;">
                      <p style="margin:0 0 24px;font-size:16px;line-height:1.9;color:#475569;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer',Arial,sans-serif;">
                        សូមប្រើប្រាស់លេខកូដផ្ទៀងផ្ទាត់ខាងក្រោម ដើម្បីបញ្ចប់ការចូលប្រើប្រាស់របស់អ្នក។ លេខកូដនេះមានសុពលភាពរយៈពេល
                        <strong style="color:#0f172a;">៥ នាទី</strong>។
                      </p>

                      <!-- OTP card -->
                      <div style="background-color:#f8fafc;border:1px dashed #cbd5e1;border-radius:14px;padding:24px 16px;text-align:center;">
                        <div style="font-size:12px;font-weight:700;color:#64748b;margin-bottom:12px;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer',Arial,sans-serif;">
                          លេខកូដផ្ទៀងផ្ទាត់របស់អ្នក
                        </div>
                        <div style="display:inline-block;background-color:#0f172a;color:#a5f3fc;font-size:24px;font-weight:800;letter-spacing:2px;padding:16px 24px;border-radius:12px;font-family:'SFMono-Regular',Consolas,'Liberation Mono',Menlo,monospace;">
                          %s
                        </div>
                      </div>

                      <!-- Warning -->
                      <div style="margin-top:24px;background-color:#fef3c7;border-left:4px solid #f59e0b;border-radius:8px;padding:14px 16px;">
                        <p style="margin:0;font-size:14px;color:#78350f;line-height:1.9;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer',Arial,sans-serif;">
                          រាល់ការចូលប្រើប្រើប្រ័ន្ធ លោកអ្នកនឹងទទួលលេខសុវត្ថិភាព។ សូមកុំចែករំលែកវាទៅអ្នកដទៃ។
                        </p>
                      </div>
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="padding:24px 28px 28px;text-align:center;border-top:1px solid #e2e8f0;margin-top:24px;">
                      <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.9;font-family:'Khmer OS Siemreab','Khmer OS','Siemreab','Kantumruy Pro','Noto Sans Khmer',Arial,sans-serif;">
                        &copy; ២០២៦ យន្តការច្រកចេញចូលតែមួយ។ រក្សាសិទ្ធិគ្រប់បែបយ៉ាង។
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(spacedOtp);
    }
    private String extractLoginIdentifier(UserRequest request) throws OwsmException {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new OwsmException("PASSWORD_REQUIRED");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail().trim();
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return request.getUsername().trim();
        }

        throw new OwsmException("USERNAME_OR_EMAIL_REQUIRED");
    }

    private boolean isCurrentUser(String username) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && username != null && username.equals(auth.getName());
    }
}