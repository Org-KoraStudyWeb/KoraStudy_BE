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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private final EmailService emailService;


    /**
     * ThienTDV - Các chức năng liên quan đến đăng nhập đăng ký
     */

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // Kiểm tra xác nhận mật khẩu
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AccountException("Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra username/email đã tồn tại
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username đã tồn tại");
        }

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email đã được đăng ký từ trước");
        }

        // Tìm role USER mặc định
        Role defaultRole = roleRepository.findByRoleName(RoleName.USER).orElseThrow(() -> new AccountException("Không tìm thấy quyền USER trong hệ thống"));

        // Tạo token xác thực email
        String verificationToken = generateVerificationToken();

        // Tạo account mới với isEnabled = false
        Account account = Account.builder().username(request.getUsername()).email(request.getEmail()).encryptedPassword(passwordEncoder.encode(request.getPassword())).roles(Set.of(defaultRole)).isEnabled(false) // Chưa kích hoạt cho đến khi xác thực email
                .emailVerificationToken(verificationToken).tokenExpiryTime(LocalDateTime.now().plusHours(24)) // Token hết hạn sau 24 giờ
                .build();

        // Tạo user kèm theo userCode tự sinh
        String userCode = userService.generateCustomerUserCode();

        User user = User.builder().userCode(userCode).email(account.getEmail()).account(account).isEnable(false) // User cũng chưa được kích hoạt
                .build();

        account.setUser(user);

        accountRepository.save(account);

        // Gửi email xác thực
        emailService.sendVerificationEmail(account.getEmail(), verificationToken);
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
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
        Account account = Account.builder().username(request.getUsername()).email(request.getJobEmail()).encryptedPassword(passwordEncoder.encode(request.getPassword())).isEnabled(true).roles(Set.of(role)).build();

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
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Sinh JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. Lấy thông tin người dùng
        AccountDetailsImpl userDetails = (AccountDetailsImpl) authentication.getPrincipal();
        List<RoleName> roles = userDetails.getAuthorities().stream().map(auth -> RoleName.valueOf(auth.getAuthority().replace("ROLE_", ""))).collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);
    }

    @Override
    public void enableAccount(Long accountId, boolean enable) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));
        account.setEnabled(enable);
        accountRepository.save(account);
    }


    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Account account = accountRepository.findAccountByUsername(username).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(oldPassword, account.getEncryptedPassword())) {
            throw new AccountException("Mật khẩu cũ không chính xác");
        }

        account.setEncryptedPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPasswordByAdmin(Long accountId, String newPassword) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy tài khoản"));

        account.setEncryptedPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    // QUÊN MẬT KHẨU
    @Override
    @Transactional
    public void forgotPassword(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new AccountException("Email không tồn tại trong hệ thống"));

        // Tạo token reset password
        String resetToken = generateResetToken();
        account.setPasswordResetToken(resetToken);
        account.setTokenExpiryTime(LocalDateTime.now().plusHours(1)); // 1 giờ

        accountRepository.save(account);

        // Gửi email reset password
        emailService.sendPasswordResetEmail(account.getEmail(), resetToken);
    }

    // RESET MẬT KHẨU
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Account account = accountRepository.findByPasswordResetToken(token).orElseThrow(() -> new AccountException("Token đặt lại mật khẩu không hợp lệ"));

        if (account.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AccountException("Token đã hết hạn");
        }

        // Cập nhật mật khẩu mới
        account.setEncryptedPassword(passwordEncoder.encode(newPassword));
        account.setPasswordResetToken(null);
        account.setTokenExpiryTime(null);

        accountRepository.save(account);
    }

    // VALIDATE RESET TOKEN
    @Override
    public void validateResetToken(String token) {
        Account account = accountRepository.findByPasswordResetToken(token).orElseThrow(() -> new AccountException("Token không hợp lệ"));

        if (account.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AccountException("Token đã hết hạn");
        }
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void verifyEmail(String token) {
        System.out.println("=== 🔍 VERIFY EMAIL START ===");
        System.out.println("📨 Token received: " + token);

        Optional<Account> accountOpt = accountRepository.findByEmailVerificationToken(token);

        if (accountOpt.isEmpty()) {
            System.out.println("ℹ️ Token không tồn tại - kiểm tra xem đã verified chưa");

            // Nếu token không tồn tại, coi như đã verified thành công
            System.out.println("✅ Coi như đã verified (token đã được sử dụng)");
            return; // Không throw exception
        }

        Account account = accountOpt.get();
        System.out.println("✅ Tìm thấy account: " + account.getEmail());

        if (account.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AccountException("Token xác thực đã hết hạn");
        }

        if (account.isEnabled()) {
            System.out.println("ℹ️ Tài khoản đã được kích hoạt - xóa token");
            account.setEmailVerificationToken(null);
            account.setTokenExpiryTime(null);
            accountRepository.save(account);
            return; // Vẫn thành công
        }

        // Kích hoạt tài khoản
        System.out.println("🎯 Đang kích hoạt tài khoản...");
        account.setEnabled(true);
        account.setEmailVerificationToken(null);
        account.setTokenExpiryTime(null);

        if (account.getUser() != null) {
            account.getUser().setEnable(true);
        }

        accountRepository.save(account);
        System.out.println("🎉 XÁC THỰC EMAIL THÀNH CÔNG cho: " + account.getEmail());
    }


    public void resendVerificationEmail(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new AccountException("Email không tồn tại"));

        if (account.isEnabled()) {
            throw new AccountException("Tài khoản đã được xác thực");
        }

        // Tạo token mới
        String newToken = generateVerificationToken();
        account.setEmailVerificationToken(newToken);
        account.setTokenExpiryTime(LocalDateTime.now().plusHours(24));

        accountRepository.save(account);

        // Gửi lại email
        emailService.sendVerificationEmail(account.getEmail(), newToken);
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
}
