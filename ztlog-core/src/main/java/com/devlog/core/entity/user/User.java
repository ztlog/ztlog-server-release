package com.devlog.core.entity.user;

import com.devlog.core.common.enumulation.UserStatus;
import com.devlog.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "user_mst")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "`GRANT`")
    private String grant;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private UserStatus status;

    /**
     * 사용자 생성
     *
     * @param userId          사용자 ID
     * @param username        사용자명
     * @param encodedPassword 암호화된 비밀번호
     * @param role            권한
     * @param status          계정 상태
     * @return User 엔티티
     */
    public static User created(String userId, String username, String encodedPassword, String role, UserStatus status) {
        return User.builder()
                .userId(userId)
                .username(username)
                .password(encodedPassword)
                .grant(role)
                .status(status)
                .build();
    }

    public void approve() {
        this.status = UserStatus.ACTIVE;
    }

}
