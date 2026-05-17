package com.devlog.core.common.utils;

import com.devlog.core.common.constants.CommonConstants;
import com.devlog.core.common.dto.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.DecodingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;


@Slf4j
@Component
public class TokenUtils {

    private static final String USER_ID = "USER_ID";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    private Key key;

    @jakarta.annotation.PostConstruct
    public void init() {
        byte[] keyBytes = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes()).getEncoded();
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * JWT 토큰 생성
     *
     * @param userId 유저 ID
     * @return JWT 토큰
     */
    public TokenInfo generateToken(String userId) {
        long now = (new Date()).getTime();
        Date accessTokenExpires = new Date(now + accessTokenExpireTime);
        Date refreshTokenExpires = new Date(now + refreshTokenExpireTime);

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(userId)
                .claim(USER_ID, userId)
                .setExpiration(accessTokenExpires)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpires)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenInfo.builder()
                .grantType(CommonConstants.BEARER_TYPE)
                .authorizationType(CommonConstants.AUTHORIZATION_HEADER)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpires.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰에서 userId 추출
     *
     * @param token 토큰
     * @return userId
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60) // 60초 clock skew 허용
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 헤더에서 userId 추출
     *
     * @param request 요청
     * @return userId
     */
    public String getUserIdFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(CommonConstants.AUTHORIZATION_HEADER);

        // 1. 유효성 검증 (Early Return)
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(CommonConstants.BEARER_PREFIX)) {
            log.warn("[TokenUtils] INVALID TOKEN ERROR: Bearer prefix missing or empty");
            throw new JwtException("INVALID_TOKEN");
        }

        log.info("[TokenUtils] Bearer Token : {}", bearerToken);

        // Prefix 제거 (상수 길이 활용 권장: CommonConstants.BEARER_PREFIX.length())
        String token = bearerToken.substring(7);
        log.info("[TokenUtils] JWT Token : {}", token);

        // 2. JWT 파싱 및 Subject 반환
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

        } catch (ExpiredJwtException e) {
            return handleJwtException("Expired", "EXPIRED_TOKEN", e);
        } catch (SecurityException | MalformedJwtException | DecodingException e) {
            return handleJwtException("Invalid", "INVALID_TOKEN", e);
        } catch (UnsupportedJwtException e) {
            return handleJwtException("Unsupported", "UNSUPPORTED_TOKEN", e);
        } catch (IllegalArgumentException e) {
            return handleJwtException("Empty", "EMPTY_TOKEN", e);
        } catch (Exception e) {
            log.error("[TokenUtils] Unhandled JWT exception", e);
            throw new JwtException("UNKNOWN_ERROR");
        }

    }

    /**
     * 토큰 검증
     *
     * @param token 토큰
     * @return 검증 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[TokenUtils] Expired JWT Token: {}", e.getMessage());
            throw new JwtException("EXPIRED_TOKEN");
        } catch (SecurityException | MalformedJwtException | DecodingException e) {
            log.warn("[TokenUtils] Invalid JWT Token: {}", e.getMessage());
            throw new JwtException("INVALID_TOKEN");
        } catch (UnsupportedJwtException e) {
            log.warn("[TokenUtils] Unsupported JWT Token: {}", e.getMessage());
            throw new JwtException("UNSUPPORTED_TOKEN");
        } catch (IllegalArgumentException e) {
            log.warn("[TokenUtils] JWT claims string is empty: {}", e.getMessage());
            throw new JwtException("EMPTY_TOKEN");
        } catch (Exception e) {
            log.error("[TokenUtils] Unhandled JWT exception", e);
            throw new JwtException("UNKNOWN_ERROR");
        }
    }

    /**
     * access 토큰 헤더에 세팅
     *
     * @param accessToken 액세스 토큰
     * @param response    응답
     */
    public void accessTokenSetHeader(String accessToken, HttpServletResponse response) {
        String headerValue = CommonConstants.BEARER_PREFIX + accessToken;
        response.setHeader(CommonConstants.AUTHORIZATION_HEADER, headerValue);
    }

    /**
     * refresh 토큰 헤더에 세팅
     *
     * @param refreshToken refresh 토큰
     * @param response     응답
     */
    public void refreshTokenSetHeader(String refreshToken, HttpServletResponse response) {
        response.setHeader("Refresh", refreshToken);
    }

    /**
     * Request 헤더에 세팅된 Access Token 추출
     *
     * @param request 요청
     * @return 토큰 정보
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(CommonConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonConstants.BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 로그 기록 및 예외 발생을 공통화 (선택 사항)
    private String handleJwtException(String type, String message, Exception e) {
        log.warn("[TokenUtils] {} JWT Token - {}", type, e.getMessage());
        throw new JwtException(message);
    }
}

