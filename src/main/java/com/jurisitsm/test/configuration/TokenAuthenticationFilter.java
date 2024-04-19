package com.jurisitsm.test.configuration;

import com.jurisitsm.test.service.TokenService;
import com.jurisitsm.test.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final TokenService tokenService;
    @Autowired
    public TokenAuthenticationFilter(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = tokenService.getAccessTokenFromRequest(request);

        Optional.ofNullable(token)
                .filter(tokenService::validateAccessToken)
                .map(tokenService::getEmailFromAccessToken)
                .map(userService::loadUserByUsername)
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
                .ifPresent(authentication -> SecurityContextHolder.getContext()
                        .setAuthentication(authentication));

        filterChain.doFilter(request, response);
    }

}
