package com.db.util;

import java.math.BigDecimal;

public class PersonalAll {
    private Integer userId;

    private String name;

    private String ssn;

    private String password;

    private BigDecimal balance;

    private Integer emailId;

    private String emailAddress;

    private Boolean isPhoneRegistered;

    private Boolean isEmailVerified;

    private String phoneNumber;

    private Boolean isEmailRegistered;

    private Boolean isPhoneVerified;


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
        this.name = name;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getEmailId() {
        return emailId;
    }

    public void setEmailId(Integer emailId) {
        this.emailId = emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Boolean getPhoneRegistered() {
        return isPhoneRegistered;
    }

    public void setPhoneRegistered(Boolean phoneRegistered) {
        isPhoneRegistered = phoneRegistered;
    }

    public Boolean getEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getEmailRegistered() {
        return isEmailRegistered;
    }

    public void setEmailRegistered(Boolean emailRegistered) {
        isEmailRegistered = emailRegistered;
    }

    public Boolean getPhoneVerified() {
        return isPhoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        isPhoneVerified = phoneVerified;
    }
}
