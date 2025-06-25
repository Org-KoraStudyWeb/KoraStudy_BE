package korastudy.be.security.userprinciple;

import korastudy.be.entity.User.Account;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/*
    ThienTDV- AccountDetailsImpl gần như là một lớp DTO chuyển đổi từ Entity Account sang UserDetails
    để Spring Security sử dụng trong quá trình xác thực (authentication) và phân quyền (authorization).
 */

@Getter
@EqualsAndHashCode(of = "id")
public class AccountDetailsImpl implements UserDetails, Serializable {

    private final Long id;
    private final String username;

    @JsonIgnore
    private final String password;

    private final String email;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor chính
    public AccountDetailsImpl(Long id, String username, String password, String email, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    // Convert từ Account Entity sang UserDetails cho Spring Security
    public static AccountDetailsImpl build(Account account) {
        List<GrantedAuthority> authorities = account.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName())).collect(Collectors.toList());

        return new AccountDetailsImpl(account.getId(), account.getUsername(), account.getEncryptedPassword(), account.getEmail(), account.isEnabled(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // có thể custom sau
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // có thể custom sau
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // có thể custom sau
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

