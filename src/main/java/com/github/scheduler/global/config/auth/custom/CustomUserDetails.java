package com.github.scheduler.global.config.auth.custom;
import com.github.scheduler.auth.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Builder
@ToString
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final UserEntity userEntity;
    private Map<String, Object> attributes;

    // 일반 로그인
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    // OAuth2 로그인
    public CustomUserDetails(UserEntity userEntity, Map<String, Object> attributes) {
        this.userEntity = userEntity;
    }

    // OAuth2 관련
    public <A> A getAttributes(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    // OAuth2 관련
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collections = new ArrayList<>();
        collections.add(() -> String.valueOf(userEntity.getRole()));

        return collections;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    /* 계정 만료 여부
     * true :  만료 안됨
     * false : 만료
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /* 계정 잠김 여부
     * true : 잠기지 않음
     * false : 잠김
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /* 비밀번호 만료 여부
     * true : 만료 안 됨
     * false : 만료
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* 사용자 활성화 여부
     * true : 활성화 됨
     * false : 활성화 안 됨
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }
}
