package com.owsm.AuthService.config;

import com.owsm.AuthService.api.JwtUtil;
import com.owsm.AuthService.securityaudit.service.AuthSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthSessionService authSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/users/register") ||
                requestURI.startsWith("/api/users/login") ||
                requestURI.startsWith("/api/users/verify-otp") ||
                requestURI.startsWith("/api/users/resend-otp") ||
                requestURI.startsWith("/api/profile") ||
                requestURI.startsWith("/api/roles") ||
                requestURI.startsWith("/api/news") ||
                requestURI.startsWith("/api/comments") ||
                requestURI.startsWith("/uploads")){

            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            UUID sessionId = jwtUtil.extractSessionId(jwt);
            UUID tokenId = jwtUtil.extractTokenId(jwt);
            boolean sessionValid = sessionId == null && tokenId == null
                    || sessionId != null
                    && tokenId != null
                    && authSessionService.isActive(sessionId, tokenId, Instant.now());
            if (jwtUtil.validateToken(jwt, userDetails) && sessionValid) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        chain.doFilter(request, response);
    }
}
