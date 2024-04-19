package com.jurisitsm.test.web.controller;

import com.jurisitsm.test.exception.ClientManagerException;
import com.jurisitsm.test.model.AppUser;
import com.jurisitsm.test.service.TokenService;
import com.jurisitsm.test.web.dto.LoginRequest;
import com.jurisitsm.test.web.dto.RefreshTokenRequest;
import com.jurisitsm.test.web.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest httpServletRequest) throws ClientManagerException {
        tokenService.blacklistAccessToken(httpServletRequest);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.email(), loginRequest.password());
        var auth = authenticationManager.authenticate(authToken);
        var user = (AppUser) auth.getPrincipal();
        var accessToken = tokenService.generateAccessTokenFromEmail(user);
        var refreshToken = tokenService.createRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken.getId()));
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal AppUser user,
                       HttpServletRequest httpServletRequest) throws ClientManagerException {
        tokenService.blacklistAccessToken(httpServletRequest);
        tokenService.deleteRefreshTokenByUser(user);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
                                             HttpServletRequest httpServletRequest) throws ClientManagerException {
        tokenService.blacklistAccessToken(httpServletRequest);
        var refreshToken = tokenService.refreshToken(refreshTokenRequest.refreshToken());
        var accessToken = tokenService.generateAccessTokenFromEmail(refreshToken.getUser());
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken.getId()));
    }

}
