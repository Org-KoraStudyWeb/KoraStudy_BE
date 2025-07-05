package korastudy.be.serv

import jakarta.transaction.Transactional;
import korastudy.be.dto.request.UpdateManagerProfileRequest;
import korastudy.be.dto.request.UserProfileUpdate;
import korastudy.be.dto.request.auth.UpdateManagerProfileRequest;
import korastudy.be.dto.request.auth.UserProfileUp
import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.NotificationRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    /**
     *
     *ThienTDV - Các chức năng liên quan đến user
     */

    //Chức năng sau khi được admin thêm account thì tự manager cập nhật profile
    @Override
    public void updateProfileAndNotify(String username, UpdateManagerProfileRequest request) {
        // Tìm Account + User
        Account account = accountRepository.findAccountByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        User user = userRepository.findByAccount(account).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy hồ sơ người dùng"));

        // Cập nhật hồ sơ
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getMyEmail());
        user.setPhoneNumber(request.getPhone());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setAvatar(request.getAvatar());
        user.setEnable(false); // yêu cầu duyệt
        userRepository.save(user);

        // Gửi thông báo đến tất cả admin
        List<Account> admins = accountRepository.findAllByRoles_RoleName(RoleName.ADMIN);

        for (Account admin : admins) {
            Notification notification = Notification.builder().title("Yêu cầu xác nhận hồ sơ mới").content("Nhân viên " + user.getLastName() + " vừa cập nhật hồ sơ cá nhân và chờ phê duyệt.").isPublished(false).user(admin.getUser()) // gắn cho admin
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Override
    public void validateUserCodeUnique(String userCode) {
        if (userRepository.existsByUserCode(userCode)) {
            throw new AlreadyExistsException("UserCode đã tồn tại");
        }
    }

    //Lấy random userCode cho user
    public String generateCustomerUserCode() {
        String userCode;
        do {
            int randomNumber = (int) (Math.random() * 9000) + 1000; // số 4 chữ số
            userCode = "CUS" + randomNumber;
        } while (userRepository.existsByUserCode(userCode));
        return userCode;
    }

    @Override
    public User createUserWithAccount(String userCode, Account account) {
        User user = User.builder().userCode(userCode).account(account).isEnable(false).build();
        return user;
    }

    @Override
    public User findByUserCode(String userCode) {
        return userRepository.findByUserCode(userCode).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy người dùng với userCode: " + userCode));
    }

    @Override
    public Optional<User> getUserByAccountUsername(String username) {
        return userRepository.findByAccount_Username(username);
    }

    /**
     *
     *Trung - Update thông tin hồ sơ của người dùng
     */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    @Override
    @Transactional
    public User updateProfile(Long userId, UserProfileUpdate dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
            if (user.getAccount() != null) {
                user.getAccount().setEmail(dto.getEmail());
            }
        }
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getDateOfBirth() != null) user.setDob(dto.getDateOfBirth());

        // Lưu account trước để tránh FK lỗi
        accountRepository.save(user.getAccount());
        return userRepository.save(user);
    }

    @Data
    public class UserProfileDTO {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String gender;
        private String avatar;
        private LocalDate dateOfBirth;
    }

    public UserProfileDTO toDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender() != null ? user.getGender().toString() : null);
        dto.setAvatar(user.getAvatar());
        dto.setDateOfBirth(user.getDob());
        dto.setEmail(
                user.getAccount() != null ? user.getAccount().getEmail() : null
        );
        return dto;
    }
}
