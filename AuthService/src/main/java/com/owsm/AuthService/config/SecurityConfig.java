package com.owsm.AuthService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, @Lazy UserDetailsService userDetailsService) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ---------- Public endpoints ----------
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/verify-otp",
                                "/api/users/resend-otp",
                                "/api/roles/**",
                                "/api/profile/**",
                                "/uploads/**"
                        ).permitAll()

                        // Allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ---------- Password change: any logged-in user ----------
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password")
                        .authenticated()

                        // ---------- Enable / disable / toggle: admins only ----------
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/*/enable",
                                "/api/users/*/disable",
                                "/api/users/*/toggle-status"
                        ).hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT")

                        // ---------- User update (PUT /api/users/{id}) ----------
                        // Allow OFFICER too so they can edit their own record
                        .requestMatchers(HttpMethod.PUT, "/api/users/*")
                        .hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT", "OFFICER")

                        // ---------- User listing / read ----------
                        .requestMatchers(
                                "/api/users/**",
                                "/api/roles/**",
                                "/api/news/**",
                                "/api/profile/**"
                        ).hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT", "OFFICER")

                        // ---------- Everything else ----------
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}