package korastudy.be.validate;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.exception.AccountException;
import korastudy.be.exception.AlreadyExistsException;


import java.util.Map;

public class UserCodeValidate {
    public static void validate(String userCode, RoleName roleName) {
        if (roleName == null || userCode == null) throw new AlreadyExistsException("Thiếu thông tin");

        Map<RoleName, String> prefixMap = Map.of(
                RoleName.ADMIN, "AD",
                RoleName.CONTENT_MANAGER,"CM",
                RoleName.DELIVERY_MANAGER,"DM"
        );

        String expectedPrefix = prefixMap.get(roleName);
        if (!userCode.startsWith(expectedPrefix)) {
            throw new AccountException("Mã userCode không hợp lệ với vai trò " + roleName);
        }
    }
}
