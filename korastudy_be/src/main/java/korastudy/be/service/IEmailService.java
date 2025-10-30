package korastudy.be.service;


import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.Account;

public interface IEmailService {
    void sendPaymentConfirmation(Account account, Course course, int amount);
}


