package com.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class RequestContribution {
    private Integer contributionId;

    private Integer requestId;

    private String senderPhoneNumber;

    private Integer senderEmailId;

    private Integer transactionId;

    @JsonProperty("contribution_amount")
    private BigDecimal contributionAmount;

    private Boolean isContributed;

    public Integer getContributionId() {
        return contributionId;
    }

    public void setContributionId(Integer contributionId) {
        this.contributionId = contributionId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        this.senderPhoneNumber = senderPhoneNumber == null ? null : senderPhoneNumber.trim();
    }

    public Integer getSenderEmailId() {
        return senderEmailId;
    }

    public void setSenderEmailId(Integer senderEmailId) {
        this.senderEmailId = senderEmailId;
    }

    public Boolean getContributed() {
        return isContributed;
    }

    public void setContributed(Boolean contributed) {
        isContributed = contributed;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    public Boolean getIsContributed() {
        return isContributed;
    }

    public void setIsContributed(Boolean isContributed) {
        this.isContributed = isContributed;
    }
}