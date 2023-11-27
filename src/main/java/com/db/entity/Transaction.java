package com.db.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
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

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Integer senderUserId) {
        this.senderUserId = senderUserId;
    }

    public Integer getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Integer getRecipientEmailId() {
        return recipientEmailId;
    }

    public void setRecipientEmailId(Integer recipientEmailId) {
        this.recipientEmailId = recipientEmailId;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber == null ? null : recipientPhoneNumber.trim();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTransactionStartTime() {
        return transactionStartTime;
    }

    public void setTransactionStartTime(Date transactionStartTime) {
        this.transactionStartTime = transactionStartTime;
    }

    public Date getTransctionFinishedTime() {
        return transctionFinishedTime;
    }

    public void setTransctionFinishedTime(Date transctionFinishedTime) {
        this.transctionFinishedTime = transctionFinishedTime;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public Date getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(Date cancelledTime) {
        this.cancelledTime = cancelledTime;
    }
}