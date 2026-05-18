package com.devlog.admin.service.user.dto.response;

import com.devlog.core.common.enumulation.UserStatus;
import com.devlog.core.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Getter
@AllArgsConstructor
public class UserDetailDto implements UserDetails {

    @Serial
    private static final long serialVersionUID = -3594757551317618173L;

    @Delegate
    private final User user;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getGrant()));
    }

    public static UserDetailDto of(User user, Collection<? extends GrantedAuthority> authorities) {
        return new UserDetailDto(user, authorities);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(user.getStatus());
    }
}
