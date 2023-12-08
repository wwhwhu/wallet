package com.db.service;

import com.db.entity.Email;
import com.db.entity.PhoneNumber;
import com.db.entity.TransactionWithBLOBs;
import com.db.entity.User;
import com.db.mapper.PhoneNumberMapper;
import com.db.util.AccountAll;
import com.db.util.PersonalAll;

import java.math.BigDecimal;
import java.util.List;

public interface UserInfoService {
    // Login Service
    int loginUserEmailService(String email, String password);
    int loginUserPhoneService(String phone, String password);
    // Register Service
    // 查询是否有手机号记录
    int getUserInfoByPhone(String phone);
    // 查询转账是否有手机号记录
    int getTransactionUserInfoByPhone(String phone);
    // 查询是否有Email记录
    int getUserInfoByEmail(String email);
    // 查询转账是否有Email记录
    Email getTransactionUserInfoByEmail(String email);
    // 获取Email ID
    int getEmailIdByEmail(String email);
    // 根据email_id查询email_address
    String getEmailByEmailId(Integer emailId);
    // 根据user_id查询email_id
    int[] getEmailIdByUserId(Integer user_id);
    // User表注册
    int registerUserService(String name, String ssn, String password, BigDecimal balance);
    // Phone表注册
    int insertPhoneService(String phone, Integer resultCode, boolean b, boolean phone_verfied);
    // Email表注册
    int insertEmailService(Integer resultCode, String emailAddress, boolean b, boolean email_verfied);
    // 寻找用户id
    int findBySSN(String ssn);
    // 更新用户信息，只可以修改姓名，密码
    int updateUserInfoService(Integer user_id, String name, String password);
    // 寻找用户密码
    User findUserPasswordService(Integer user_id);
    // Email表更新
    int updateEmailService(Integer user_id, String email, boolean v, boolean r);
    // Email删除
    int deleteEmailService(Integer user_id, String email);
    // Phone表更新
    int updatePhoneService(Integer user_id, String phoneNumber, boolean v, boolean r);
    // Phone删除
    int deletePhoneService(Integer user_id, String phoneNumber);
    // 添加BankAccount，返回AccountID
    int insertBankAccountService(String bankId, String accountNumber, Boolean isJoint);
    // 添加UserBankAccount,返回UserBankAccountID
    int insertUserBankAccountService(Integer userId, Integer bankAccountId, Boolean isPrimary, Boolean isVerified);
    // 删除UserBankAccount
    int deleteUserBankAccountService(Integer userId, Integer bankAccountId);
    // 根据id查找Account
    List<Integer> getAccountByUserId(Integer userId);
    // 一次性获取所有的Account信息
    List<AccountAll> getAccountAllInfoByUserId(Integer userId);
    // 一次性获取所有的个人信息
    List<PersonalAll> getPersonalInfoByUserId(Integer userId);
    // 改变Primary Account
    int changePrimaryAccountService(Integer userId, Integer account_id);
    // 根据user_id获取balance
    BigDecimal getBalanceByUserId(Integer userId);
    // 根据user_id获取手机号
    String getPhoneByUserId(Integer userId);
    // 根据user_id获取用户名
    String getNameByUserId(Integer userId);
    // 根据user_id更新balance
    int updateBalanceByUserId(Integer userId, BigDecimal balance);
    // 根据phone获取所有的未完成Transaction
    List<TransactionWithBLOBs> getForePhoneBalance(String phone);
    // 根据email获取所有的未完成Transaction
    List<TransactionWithBLOBs> getForeEmailBalance(Integer emailID);
    // 记录删除的Email的Id
    int deleteEmailRecordService(int deleteEmail, String emailAddress);
}
