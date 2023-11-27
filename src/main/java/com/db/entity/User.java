package com.db.entity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public class User {
    private Integer userId;

    private String name;

    private String ssn;

    private String password;

    private BigDecimal balance;

    private Email email;

    private PhoneNumber phone;

    private UserBankAccount userBankAccount;

    private BankAccount bankAccount;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn == null ? null : ssn.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}