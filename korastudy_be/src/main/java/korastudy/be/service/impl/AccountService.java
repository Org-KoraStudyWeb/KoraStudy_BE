package korastudy.be.service.impl;

import korastudy.be.dto.request.LoginRequest;
import korastudy.be.dto.request.RegisterRequest;
import korastudy.be.dto.response.JwtResponse;
import korastudy.be.repository.AccountRepository;
import korastudy.be.service.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;

    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public String resolveHomePageByRole(String userName) {
        return "";
    }

    @Override
    public void enableAccount(Long accountId, boolean enable) {

    }

    @Override
    public void assignRole(Long accountId, String roleName) {

    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {

    }

    @Override
    public void resetPasswordByAdmin(Long accountId, String newPassword) {

    }
}
