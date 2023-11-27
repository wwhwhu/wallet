package com.db.entity;

public class TransactionWithBLOBs extends Transaction {
    private String memo;

    private String cancelledReason;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason == null ? null : cancelledReason.trim();
    }
}