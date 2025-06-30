package korastudy.be.service;

import korastudy.be.dto.request.UpdateManagerProfileRequest;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;

import java.util.Optional;

public interface IUserService {

    void updateProfileAndNotify(String username, UpdateManagerProfileRequest request);

    void validateUserCodeUnique(String userCode);

    User createUserWithAccount(String userCode, Account account);

    User findByUserCode(String userCode);

    Optional<User> getUserByAccountUsername(String userName);
}
