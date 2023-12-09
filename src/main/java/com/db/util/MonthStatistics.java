package com.db.util;

import com.db.entity.TransactionWithBLOBs;

import java.math.BigDecimal;
import java.util.List;

public class MonthStatistics {
    // 年
    int year;
    // 月
    int month;
    // 最大交易额以及对应的交易ID
    List<Integer> maxID;
    BigDecimal maxAmount;
    // 平均交易额
    BigDecimal averageAmount;
    // 交易总额
    BigDecimal totalAmount;
    // 交易总次数
    int totalTimes;

    public List<Transinfo> getMonthStatisticsList() {
        return monthStatisticsList;
    }

    public void setMonthStatisticsList(List<Transinfo> monthStatisticsList) {
        this.monthStatisticsList = monthStatisticsList;
    }

    // 交易记录
    List<Transinfo> monthStatisticsList;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public List<Integer> getMaxID() {
        return maxID;
    }

    public void setMaxID(List<Integer> maxID) {
        this.maxID = maxID;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(int totalTimes) {
        this.totalTimes = totalTimes;
    }
}
