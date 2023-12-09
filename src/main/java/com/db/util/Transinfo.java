package com.db.util;

import com.db.entity.TransactionWithBLOBs;

import java.math.BigDecimal;
import java.util.Date;

public class Transinfo extends TransactionWithBLOBs {
    private Integer transactionId;

    private Integer senderUserId;

    private Integer recipientUserId;

    private Integer recipientEmailId;

    private String recipientPhoneNumber;

    private BigDecimal amount;

    private Date transactionStartTime;

    private Date transctionFinishedTime;

    private Boolean isCancelled;

    private Date cancelledTime;

    private String userName;

    private String emailAddress;

    private String memo;

    private String cancelledReason;

    @Override
    public Integer getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public Integer getSenderUserId() {
        return senderUserId;
    }

    @Override
    public void setSenderUserId(Integer senderUserId) {
        this.senderUserId = senderUserId;
    }

    @Override
    public Integer getRecipientUserId() {
        return recipientUserId;
    }

    @Override
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    @Override
    public Integer getRecipientEmailId() {
        return recipientEmailId;
    }

    @Override
    public void setRecipientEmailId(Integer recipientEmailId) {
        this.recipientEmailId = recipientEmailId;
    }

    @Override
    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    @Override
    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public Date getTransactionStartTime() {
        return transactionStartTime;
    }

    @Override
    public void setTransactionStartTime(Date transactionStartTime) {
        this.transactionStartTime = transactionStartTime;
    }

    @Override
    public Date getTransctionFinishedTime() {
        return transctionFinishedTime;
    }

    @Override
    public void setTransctionFinishedTime(Date transctionFinishedTime) {
        this.transctionFinishedTime = transctionFinishedTime;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public Date getCancelledTime() {
        return cancelledTime;
    }

    @Override
    public void setCancelledTime(Date cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String getMemo() {
        return memo;
    }

    @Override
    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Override
    public String getCancelledReason() {
        return cancelledReason;
    }

    @Override
    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }
}
