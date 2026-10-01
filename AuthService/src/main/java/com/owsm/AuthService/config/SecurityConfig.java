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

    private final   JwtRequestFilter jwtRequestFilter;

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
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/verify-otp",
                                "/api/users/resend-otp",
                                "/api/roles/**",
                                "/api/profile/**",
                                "/uploads/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-access/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/menu-access/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/sidebar-menus")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/sidebar-menus")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sidebar-menus/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*/menu-access")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/menu-access")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/menu-access")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/locations/**")
                        .hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT")
                        .requestMatchers(HttpMethod.PUT, "/api/locations/**")
                        .hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/locations/**")
                        .hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET, "/api/locations/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/logout")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password")
                        .authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/*/enable",
                                "/api/users/*/disable",
                                "/api/users/*/toggle-status"
                        ).hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*")
                        .hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT", "OFFICER")
                        .requestMatchers(
                                "/api/users/**",
                                "/api/roles/**",
                                "/api/news/**",
                                "/api/profile/**"
                        ).hasAnyAuthority("ADMIN", "HEAD_OF_DEPARTMENT", "OFFICER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}