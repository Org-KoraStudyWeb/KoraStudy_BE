package korastudy.be.service.impl;

import korastudy.be.dto.request.CreateAccountRequest;
import korastudy.be.dto.request.LoginRequest;
import korastudy.be.dto.request.RegisterRequest;
import korastudy.be.dto.response.JwtResponse;
import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.Role;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AccountException;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.RoleRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.security.jwt.JwtUtils;
import korastudy.be.service.IAccountService;
import korastudy.be.validate.UserCodeValidate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserService userService;
    private final NotificationService notificationService;


    /*
    ThienTDV - Các chức năng liên quan đến đăng nhập đăng ký
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
                .phoneNumber(request.getPhoneNumber())
                .encryptedPassword(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(defaultRole))
                .isEnabled(true)
                .build();
        // 5. Tạo user kèm theo userCode tự sinh
        String userCode = userService.generateCustomerUserCode();

        User user = User.builder()
                .userCode(userCode)
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
