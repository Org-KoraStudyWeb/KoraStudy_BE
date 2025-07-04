package korastudy.be.service;

import korastudy.be.dto.request.auth.UpdateManagerProfileRequest;
import korastudy.be.dto.request.auth.UserProfileUpdate;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.service.impl.UserService;

import java.util.Optional;

public interface IUserService {

    void updateProfileAndNotify(String username, UpdateManagerProfileRequest request);

    void validateUserCodeUnique(String userCode);

    User createUserWithAccount(String userCode, Account account);

    User findByUserCode(String userCode);

    Optional<User> getUserByAccountUsername(String userName);

    /**
     * Trung - Update thông tin hồ sơ của người dùng
     */
    User updateProfile(Long userId, UserProfileUpdate dto);

    User getUserById(Long userId);

    UserService.UserProfileDTO toDTO(User user);

}
