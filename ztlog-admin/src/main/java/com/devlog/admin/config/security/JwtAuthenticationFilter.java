package com.devlog.admin.config.security;

import com.devlog.core.common.constants.CommonConstants;
import com.devlog.core.common.utils.TokenUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = tokenUtils.resolveAccessToken(request);

        if (StringUtils.hasText(token)) {
            try {
                this.tokenUtils.validateToken(token);
                String userId = tokenUtils.getUserIdFromToken(token);
                authenticate(userId, request);
            } catch (JwtException e) {
                if (CommonConstants.EXPIRED.equals(e.getMessage())) {
                    if (tokenUtils.reissue(request, response)) {
                        String refreshToken = request.getHeader(CommonConstants.REFRESH_HEADER);
                        String reissuedUserId = tokenUtils.getUserIdFromToken(refreshToken);

                        // 재발급된 정보로 즉시 인증 처리 후 필터 체인 통과 및 종료
                        authenticate(reissuedUserId, request);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
                request.setAttribute("exception", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String userId, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        if (userDetails != null && userDetails.isEnabled()) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
