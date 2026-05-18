package com.devlog.admin.service.user;

import com.devlog.admin.service.user.dto.request.LoginReqDto;
import com.devlog.admin.service.user.dto.request.SignupReqDto;
import com.devlog.admin.service.user.dto.response.UserDetailDto;
import com.devlog.admin.service.user.dto.response.UserResDto;
import com.devlog.core.common.dto.TokenInfo;
import com.devlog.core.common.enumulation.ResponseCode;
import com.devlog.core.common.enumulation.UserRole;
import com.devlog.core.common.utils.TokenUtils;
import com.devlog.core.config.exception.DataConflictException;
import com.devlog.core.config.exception.DataNotFoundException;
import com.devlog.core.config.exception.InternalServerException;
import com.devlog.core.entity.user.User;
import com.devlog.core.repository.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final PasswordEncoder passwordEncoder;


    /**
     * 사용자 정보 조회
     *
     * @param request HTTP 요청 객체
     * @return UserResDto
     */
    @Transactional(readOnly = true)
    public UserResDto getUserInfo(HttpServletRequest request) {
        try {
            // 2. 헤더에서 유저 ID 추출 (내부에서 인코딩 문제 여부 확인 필요)
            String userId = tokenUtils.getUserIdFromHeader(request);

            // 3. 유저 조회 및 DTO 변환 (Optional 사용 최적화)
            return userRepository.findOptionalByUserId(userId)
                    .map(UserResDto::of)
                    .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));

        } catch (JwtException e) {
            log.error("[TokenService] JWT exception in service layer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TokenService] Unexpected error while retrieving user info", e);
            throw new InternalServerException("사용자 정보 조회 중 알 수 없는 오류가 발생했습니다.");
        }

    }

    /**
     * 회원가입
     *
     * @param reqDto 회원가입 요청 정보
     */
    public void signupUser(SignupReqDto reqDto) {
        // 중복 사용자 검증
        if (userRepository.existsByUserId(reqDto.getUserId())) {
            throw new DataConflictException(ResponseCode.CONFLICT_USER_ERROR.getMessage());
        }

        // TODO : 최상위 어드민 권한 허용 로직 추가

        // 비밀번호 암호화 및 사용자 생성
        String encodedPassword = passwordEncoder.encode(reqDto.getPassword());
        User user = User.created(reqDto.getUserId(), reqDto.getUsername(), encodedPassword, UserRole.ADMIN.value());

        userRepository.save(user);
    }

    /**
     * 로그인
     *
     * @param reqDto 로그인 요청 정보
     * @return TokenInfo JWT 토큰 정보
     */
    public TokenInfo loginUser(LoginReqDto reqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqDto.getUserId(), reqDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (authentication.getPrincipal() instanceof UserDetailDto userDetailDto) {
                return tokenUtils.generateToken(userDetailDto.getUserId());
            }

            throw new InternalServerException(ResponseCode.INTERNAL_SERVER_ERROR.getMessage());
        } catch (AuthenticationException e) {
            throw new DataNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 로그아웃
     *
     * @param request HTTP 요청
     */
    public void logoutUser(HttpServletRequest request) {
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 회원탈퇴
     *
     * @param request HTTP 요청 객체
     */
    public void withdrawUser(HttpServletRequest request) {
        // 현재 인증된 사용자 정보 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InternalServerException(ResponseCode.UNAUTHORIZED_USER_GRANT.getMessage());
        }

        String userId = authentication.getName();
        User user = userRepository.findOptionalByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.NOT_FOUND_DATA.getMessage()));

        // 사용자 삭제
        userRepository.delete(user);

        // 로그아웃 처리
        logoutUser(request);
    }

}
