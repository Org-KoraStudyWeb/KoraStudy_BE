package korastudy.be.service;


import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.Account;

public interface IEmailService {

    void sendVerificationEmail(String toEmail, String verificationToken);

    void sendPasswordResetEmail(String toEmail, String resetToken);

    void sendPaymentConfirmation(Account account, Course course, int amount);
}


