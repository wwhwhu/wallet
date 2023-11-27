package com.db.entity;

public class BankAccount {
    private Integer accountId;

    private String bankId;

    private String accountNumber;

    private Boolean isJoint;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId == null ? null : bankId.trim();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber == null ? null : accountNumber.trim();
    }

    public Boolean getIsJoint() {
        return isJoint;
    }

    public void setIsJoint(Boolean isJoint) {
        this.isJoint = isJoint;
    }
}