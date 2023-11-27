package com.db.mapper;

import com.db.entity.BankAccount;

public interface BankAccountMapper {
    int deleteByPrimaryKey(Integer accountId);

    int insert(BankAccount record);

    int insertSelective(BankAccount record);

    BankAccount selectByPrimaryKey(Integer accountId);

    int updateByPrimaryKeySelective(BankAccount record);

    int updateByPrimaryKey(BankAccount record);

    BankAccount selectByAccountNum(String accountId);
}