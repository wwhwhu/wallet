package com.db.mapper;

import com.db.entity.User;
import com.db.entity.UserBankAccount;
import com.db.util.AccountAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserBankAccountMapper {
    int deleteByPrimaryKey(Integer userAccountId);

    int insert(UserBankAccount record);

    int insertSelective(UserBankAccount record);

    UserBankAccount selectByPrimaryKey(Integer userAccountId);

    int updateByPrimaryKeySelective(UserBankAccount record);

    int updateByPrimaryKey(UserBankAccount record);

    int changePrimaryAccount(UserBankAccount record);

    List<Integer> searchUserAccount(Integer user_id);

    List<Integer> searchAccountUser(Integer account_id);

    int selectByUserAccount(UserBankAccount record);

    int deleteByUserAccount(UserBankAccount record);

    List<AccountAll> searchAllAccountInfo(Integer user_id);

    Boolean isPrimaryAccount(UserBankAccount record);
}