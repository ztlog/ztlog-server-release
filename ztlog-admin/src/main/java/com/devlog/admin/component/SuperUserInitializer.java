package com.devlog.admin.component;

import com.devlog.core.common.enumulation.UserRole;
import com.devlog.core.common.enumulation.UserStatus;
import com.devlog.core.entity.user.User;
import com.devlog.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.super-user.id}")
    private String superUserId;

    @Value("${admin.super-user.password}")
    private String superUserPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUserId(superUserId)) {
            String encoded = passwordEncoder.encode(superUserPassword);
            User superUser = User.created(superUserId, superUserId, encoded, UserRole.ADMIN.value(), UserStatus.ACTIVE);
            userRepository.save(superUser);
            log.info("[SuperUserInitializer] 슈퍼유저 계정 생성: {}", superUserId);
        }
    }
}
