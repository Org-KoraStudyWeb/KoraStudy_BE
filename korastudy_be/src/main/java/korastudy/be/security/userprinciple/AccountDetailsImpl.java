package korastudy.be.security.userprinciple;

import korastudy.be.entity.User.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public AccountDetailsImpl(Long id,
                              String username,
                              String password,
                              String email,
                              boolean enabled,
                              Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    // 🟢 Chuyển từ Entity sang UserDetails
    public static AccountDetailsImpl build(Account account) {
        List<GrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        return new AccountDetailsImpl(
                account.getId(),
                account.getUsername(),
                account.getEncryptedPassword(),
                account.getEmail(),
                account.isEnabled(),
                authorities
        );
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
