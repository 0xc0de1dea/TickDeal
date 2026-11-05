package com.example.tickdeal.common.filter;

import com.example.tickdeal.common.enums.UserRoleEnum;
import com.example.tickdeal.common.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServlet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.example.tickdeal.common.constant.Constants.AUTH_HEADER;
import static com.example.tickdeal.common.constant.Constants.BEARER_PREFIX;

@Slf4j(topic = "JwtFilter")
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTH_HEADER);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader
                .substring(BEARER_PREFIX.length());

        if (!jwtUtil.validateAccessToken(jwt)) {
            send(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"error\":\"Invalid access token claims\"}");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(jwt);
            String role = jwtUtil.extractRole(jwt);

            UserRoleEnum roleEnum = UserRoleEnum.valueOf(role);

            User user = new User(
                    email,
                    "",
                    List.of(roleEnum::getRole)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            send(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"error\":\"Invalid access token claims\"}");
        }
    }

    private void send(HttpServletResponse response, int status, String context) throws IOException {
        SecurityContextHolder.clearContext();

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(context);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/auth/reissue")
                || path.equals("/api/auth/logout");
    }
}
