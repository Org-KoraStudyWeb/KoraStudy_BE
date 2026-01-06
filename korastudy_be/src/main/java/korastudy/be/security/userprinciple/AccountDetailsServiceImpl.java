package korastudy.be.security.userprinciple;

import korastudy.be.entity.User.Account;
import korastudy.be.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
    ThienTDV - một lớp custom triển khai UserDetailsService – bắt buộc phải có nếu bạn dùng Spring Security
    với xác thực dựa trên username.
 */

@Service
@RequiredArgsConstructor
public class AccountDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findActiveAccountByUsernameOrEmail(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        return AccountDetailsImpl.build(account);
    }
}

