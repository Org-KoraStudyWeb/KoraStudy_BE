package korastudy.be.service.impl;

import korastudy.be.dto.request.auth.CreateAccountRequest;
import korastudy.be.dto.request.auth.LoginRequest;
import korastudy.be.dto.request.auth.RegisterRequest;
import korastudy.be.dto.response.auth.JwtResponse;
import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.Role;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AccountException;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.RoleRepository;
import korastudy.be.security.jwt.JwtUtils;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IAccountService;
import korastudy.be.validate.UserCodeValidate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final UserService userService;
    private final NotificationService notificationService;


    /**
     * ThienTDV - Các chức năng liên quan đến đăng nhập đăng ký
     */

    @Override
    public void register(RegisterRequest request) {
        //Kiểm tra xác nhận mật khẩu
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AccountException("Mật khẩu xác nhận không khớp");
        }

        //Kiểm tra username/email đã tồn tại
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username đã tồn tại");
        }

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email đã được đăng ký từ trước");
        }
        //Tìm role USER mặc định
        Role defaultRole = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new AccountException("Không tìm thấy quyền USER trong hệ thống"));

        //Tạo account mới
        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .encryptedPassword(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(defaultRole))
                .isEnabled(true)
                .build();
        // 5. Tạo user kèm theo userCode tự sinh
        String userCode = userService.generateCustomerUserCode();

        User user = User.builder()
                .userCode(userCode)
                .email(account.getEmail())
                .account(account)
                .isEnable(true)
                .build();

        account.setUser(user);

        accountRepository.save(account);
    }

    //Admin thêm tài khoản cho các role quản lý
    @Override
    public void createInternalAccount(CreateAccountRequest request) {
        // 1. Kiểm tra trùng username/email
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username đã tồn tại");
        }

        if (accountRepository.existsByEmail(request.getJobEmail())) {
            throw new AlreadyExistsException("Email đã tồn tại");
        }

        // 2. Check userCode trùng
        userService.validateUserCodeUnique(request.getUserCode());

        // 3. Lấy role từ RoleService
        RoleName roleName = request.getRoleName(); // Enum
        Role role = roleService.getRoleByName(roleName);

        // 4. Validate userCode phù hợp với role (AD, CM, DM)
        UserCodeValidate.validate(request.getUserCode(), roleName);

        // 5. Tạo account
        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getJobEmail())
                .encryptedPassword(passwordEncoder.encode(request.getPassword()))
                .isEnabled(true)
                .roles(Set.of(role))
                .build();

        // 6. Tạo user từ UserService
        User user = userService.createUserWithAccount(request.getUserCode(), account);

        // 7. Gán và lưu
        account.setUser(user);
        accountRepository.save(account);

        // 8. Tạo thông báo yêu cầu cập nhật hồ sơ
        notificationService.notifyProfileRequired(user, roleName.name());
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        // 1. Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Sinh JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. Lấy thông tin người dùng
        AccountDetailsImpl userDetails = (AccountDetailsImpl) authentication.getPrincipal();
        List<RoleName> roles = userDetails.getAuthorities().stream()
                .map(auth -> RoleName.valueOf(auth.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);
    }

    @Override
    public void enableAccount(Long accountId, boolean enable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));
        account.setEnabled(enable);
        accountRepository.save(account);
    }


    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(oldPassword, account.getEncryptedPassword())) {
            throw new AccountException("Mật khẩu cũ không chính xác");
        }

        account.setEncryptedPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPasswordByAdmin(Long accountId, String newPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));

        account.setEncryptedPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
