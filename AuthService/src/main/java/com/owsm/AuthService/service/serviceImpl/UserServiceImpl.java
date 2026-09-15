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
    private void sendOtpEmail(String to, String otp) {
        log.info("Attempting to send OTP to {} (code: {})", to, otp);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject("OWSM · Your Verification Code (" + otp + ")");
            helper.setText(buildOtpPlainText(otp), buildOtpHtml(otp));

            mailSender.send(message);
            log.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", to, e.getMessage(), e);
        }
    }

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
    private String buildOtpHtml(String otp) {
        // Build OTP digit boxes
        StringBuilder otpBoxes = new StringBuilder();
        for (char c : otp.toCharArray()) {
            otpBoxes.append("""
            <td align="center" style="padding: 0 4px;">
              <div class="otp-digit-box" style="width:44px;height:54px;line-height:54px;background-color:#ffffff;border:2px solid #e2e8f0;border-radius:12px;font-size:26px;font-weight:700;color:#0f172a;font-family:'SF Mono',Consolas,Monaco,monospace;text-align:center;box-shadow:0 2px 6px rgba(15,23,42,0.06);">
                %s
              </div>
            </td>
            """.formatted(c));
        }

        return """
        <!DOCTYPE html>
        <html lang="km" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <meta name="x-apple-disable-message-reformatting" />
          <meta name="color-scheme" content="light dark" />
          <meta name="supported-color-schemes" content="light dark" />
          <title>លេខកូដផ្ទៀងផ្ទាត់ OWSM</title>
          <link href="https://fonts.googleapis.com/css2?family=Kantumruy+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
          <style>
            body, table, td, p, a, li, blockquote {
              -webkit-text-size-adjust: 100%%;
              -ms-text-size-adjust: 100%%;
            }
            table, td {
              mso-table-lspace: 0pt;
              mso-table-rspace: 0pt;
            }
            img { -ms-interpolation-mode: bicubic; border: 0; height: auto; line-height: 100%%; outline: none; text-decoration: none; }
            body {
              margin: 0 !important;
              padding: 0 !important;
              width: 100%% !important;
              background-color: #f1f5f9;
              -webkit-font-smoothing: antialiased;
              -moz-osx-font-smoothing: grayscale;
            }
            * {
              font-family: 'Kantumruy Pro', 'Khmer OS Siemreab', 'Siemreab', 'Noto Sans Khmer', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            }
            /* Dark mode adaptations */
            @media (prefers-color-scheme: dark) {
              .email-container   { background-color:#1e293b !important; border-color:#334155 !important; }
              .brand-header      { background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%) !important; }
              .body-text         { color:#cbd5e1 !important; }
              .otp-digit-box     { background-color:#0f172a !important; border-color:#334155 !important; color:#f8fafc !important; }
              .otp-section-bg    { background-color:#0f172a !important; border-color:#334155 !important; }
              .security-warning  { background-color:#2d1b0e !important; border-color:#78350f !important; }
              .security-warning-text { color:#fcd34d !important; }
              .footer-text       { color:#64748b !important; border-top-color:#334155 !important; }
              .footer-copyright  { color:#475569 !important; }
              .otp-label         { color:#94a3b8 !important; }
              .main-heading      { color:#f1f5f9 !important; }
              .badge-text        { color:#7dd3fc !important; }
              .body-strong       { color:#f1f5f9 !important; }
            }
            /* Mobile responsiveness */
            @media only screen and (max-width: 480px) {
              .email-container   { width:100%% !important; border-radius:14px !important; }
              .padding-mobile    { padding-left:20px !important; padding-right:20px !important; }
              .brand-header      { padding: 28px 20px 22px 20px !important; }
              .otp-digit-box     { width:36px !important; height:46px !important; line-height:46px !important; font-size:20px !important; }
            }
          </style>
        </head>
        <body style="margin:0; padding:0; background-color:#f1f5f9; color:#0f172a;">

          <!-- Preheader (hidden inbox preview text) -->
          <div style="display:none; font-size:1px; color:#f1f5f9; line-height:1px; max-height:0; max-width:0; opacity:0; overflow:hidden;">
            លេខកូដផ្ទៀងផ្ទាត់ OWSM របស់អ្នក — មានសុពលភាព ៥ នាទី
          </div>

          <!-- Main Wrapper -->
          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f1f5f9; padding: 32px 16px;">
            <tr>
              <td align="center">

                <!-- Email Container -->
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="email-container" style="max-width:520px; background-color:#ffffff; border-radius:20px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 25px 50px -12px rgba(15,23,42,0.15);">

                  <!-- Brand Header -->
                  <tr>
                    <td align="center" class="brand-header" style="padding:40px 36px 28px 36px; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); text-align:center;">
                      <table role="presentation" align="center" cellpadding="0" cellspacing="0" border="0" style="margin:0 auto;">
                        <tr>
                          <td align="center" style="background: rgba(56,189,248,0.12); border:1.5px solid rgba(56,189,248,0.30); border-radius:24px; padding:7px 20px;">
                            <span class="badge-text" style="color:#38bdf8; font-size:11px; font-weight:700; letter-spacing:2px; text-transform:uppercase; display:inline-block;">OWSM System</span>
                          </td>
                        </tr>
                      </table>

                      <h1 class="main-heading" style="margin:18px 0 0 0; color:#ffffff; font-size:22px; font-weight:700; line-height:1.4; text-align:center; letter-spacing:-0.3px;">
                        យន្តការច្រកចេញចូលតែមួយ
                      </h1>

                      <table role="presentation" align="center" cellpadding="0" cellspacing="0" border="0" style="margin:16px auto 0 auto;">
                        <tr>
                          <td style="width:40px; height:3px; background: linear-gradient(90deg, transparent, #38bdf8, transparent); border-radius:2px;"></td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- Main Content -->
                  <tr>
                    <td class="padding-mobile" style="padding:36px 40px 28px 40px;">
                      <p class="body-text" style="margin:0 0 28px 0; font-size:15px; line-height:1.8; color:#475569; text-align:center;">
                        សូមប្រើប្រាស់លេខកូដផ្ទៀងផ្ទាត់ខាងក្រោម ដើម្បីបញ្ចប់ការចូលប្រើប្រាស់គណនីរបស់អ្នក។ លេខកូដនេះមានសុពលភាពរយៈពេល <strong class="body-strong" style="color:#0f172a; font-weight:700;">៥ នាទី</strong> ប៉ុណ្ណោះ។
                      </p>

                      <!-- OTP Digit Display -->
                      <div class="otp-section-bg" style="background-color:#f8fafc; border-radius:16px; padding:28px 16px 24px 16px; margin-bottom:28px; border:1px solid #f1f5f9;">
                        <p class="otp-label" style="margin:0 0 16px 0; font-size:11px; font-weight:700; color:#64748b; text-align:center; text-transform:uppercase; letter-spacing:1.5px;">
                          លេខកូដផ្ទៀងផ្ទាត់របស់អ្នក
                        </p>
                        <table role="presentation" align="center" cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            %s
                          </tr>
                        </table>
                      </div>

                      <!-- Security Warning -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="security-warning" style="background-color:#fffbeb; border:1.5px solid #fde68a; border-radius:12px;">
                        <tr>
                          <td width="24" valign="top" style="padding:16px 12px 16px 18px; font-size:18px; line-height:1;">
                            🔒
                          </td>
                          <td class="security-warning-text" style="padding:16px 18px 16px 0; font-size:13px; line-height:1.7; color:#92400e;">
                            <strong style="font-weight:700;">ការព្រមានអំពីសុវត្ថិភាព:</strong> សូមកុំចែករំលែកលេខកូដនេះទៅកាន់អ្នកផ្សេងឱ្យសោះ រួមទាំងបុគ្គលិក OWSM ផងដែរ។
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td class="padding-mobile footer-text" style="padding:24px 40px 36px 40px; border-top:1px solid #f1f5f9; text-align:center;">
                      <p style="margin:0 0 8px 0; font-size:12px; color:#94a3b8; line-height:1.6;">
                        អ៊ីមែលនេះត្រូវបានផ្ញើដោយស្វ័យប្រវត្តិ សូមកុំឆ្លើយតបមកកាន់អ៊ីមែលនេះ។
                      </p>
                      <p class="footer-copyright" style="margin:0; font-size:12px; color:#cbd5e1;">
                        &copy; ២០២៦ យន្តការច្រកចេញចូលតែមួយ (OWSM)។ រក្សាសិទ្ធិគ្រប់យ៉ាង។
                      </p>
                    </td>
                  </tr>

                </table>

              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(otpBoxes.toString());
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