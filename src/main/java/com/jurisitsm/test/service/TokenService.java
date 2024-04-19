package com.jurisitsm.test.service;

import com.jurisitsm.test.exception.ClientManagerException;
import com.jurisitsm.test.model.AppUser;
import com.jurisitsm.test.model.RefreshToken;
import com.jurisitsm.test.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class TokenService {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int ACCESS_TOKEN_EXPIRATION = 60;
    private static final int REFRESH_TOKEN_EXPIRATION = 120;
    private static final String SECRET = "SomeRandomSecretKeyThatIsHopefullyLongEnough";
    private final RefreshTokenRepository refreshTokenRepository;
    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    public TokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessTokenFromEmail(AppUser user) {
        if (!StringUtils.hasText(user.getEmail())) {
            return null;
        }

        var key = getSecretKey();
        LocalDateTime issueDate = LocalDateTime.now();
        var expirationDate = issueDate.plusMinutes(ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(issueDate.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public RefreshToken createRefreshToken(AppUser user) throws ClientManagerException {
        if (user == null) {
            throw new ClientManagerException("Missing user.", HttpStatus.UNAUTHORIZED);
        }
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now()
                .plusMinutes(REFRESH_TOKEN_EXPIRATION);
        RefreshToken refreshToken = new RefreshToken(refreshTokenExpiryDate, user);
        return refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshTokenByUser(AppUser user) throws ClientManagerException {
        if (user == null) {
            throw new ClientManagerException("Missing user on logout.", HttpStatus.UNAUTHORIZED);
        }
        refreshTokenRepository.deleteByUser(user);
    }

    public boolean validateAccessToken(String accessToken) {
        if (blacklist.contains(accessToken)) {
            return false;
        }
        try {
            var key = getSecretKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        var token = authHeader.replace(TOKEN_PREFIX, "");
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return token;
    }

    public String getEmailFromAccessToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }
        var key = getSecretKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject();
    }

    public boolean isRefreshTokenExpired(RefreshToken token) throws ClientManagerException {
        if (token == null) {
            throw new ClientManagerException("Missing Refresh token", HttpStatus.BAD_REQUEST);
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            return true;
        }

        return false;
    }

    public RefreshToken refreshToken(String refreshTokenId) throws ClientManagerException {
        RefreshToken existingRefreshToken = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new ClientManagerException("Refresh token with given id could not be found",
                        HttpStatus.NOT_FOUND));
        if (isRefreshTokenExpired(existingRefreshToken)) {
            throw new ClientManagerException("Refresh token is already expired.", HttpStatus.BAD_REQUEST);
        }
        return createRefreshToken(existingRefreshToken.getUser());
    }


    public void blacklistAccessToken(HttpServletRequest request) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            return;
        }
        var token = authHeader.replace(TOKEN_PREFIX, "");
        if (!StringUtils.hasText(token)) {
            return;
        }
        blacklist.add(token);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}
