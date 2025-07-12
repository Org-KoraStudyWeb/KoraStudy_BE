package korastudy.be.dto.response.account;

import korastudy.be.entity.User.Account;
import lombok.Data;

@Data
public class AccountResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public static AccountResponse fromEntity(Account account) {
        AccountResponse res = new AccountResponse();
        res.setId(account.getId());
        res.setUsername(account.getUsername());
        if (account.getUser() != null) {
            res.setFirstName(account.getUser().getFirstName());
            res.setLastName(account.getUser().getLastName());
        }
        return res;
    }
}
