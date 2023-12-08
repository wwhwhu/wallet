package com.db.service.impl;

import com.db.entity.*;
import com.db.mapper.*;
import com.db.service.UserInfoService;
import com.db.util.AccountAll;
import com.db.util.PersonalAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    private final UserMapper userMapper;
    private final PhoneNumberMapper phoneNumber;
    private final EmailMapper emailMapper;
    private final BankAccountMapper bankAccountMapper;
    private final UserBankAccountMapper userBankAccountMapper;
    private final TransactionMapper transactionMapper;
    private final DeleteEmailMapper deleteEmailMapper;

    @Autowired
    public UserInfoServiceImpl(UserMapper userMapper, PhoneNumberMapper phoneNumber, EmailMapper emailMapper, BankAccountMapper bankAccountMapper, UserBankAccountMapper userBankAccountMapper, TransactionMapper transactionMapper, DeleteEmailMapper deleteEmailMapper){
        this.userMapper = userMapper;
        this.phoneNumber = phoneNumber;
        this.emailMapper = emailMapper;
        this.bankAccountMapper = bankAccountMapper;
        this.userBankAccountMapper = userBankAccountMapper;
        this.transactionMapper = transactionMapper;
        this.deleteEmailMapper = deleteEmailMapper;
    }

    @Override
    public int loginUserEmailService(String email, String password) {
        User res = userMapper.selectByEmail(email);
        if (res == null) {
            return -1;
        }
        if (res.getPassword().equals(password)) {
            return res.getUserId();
        }
        return -2;
    }

    @Override
    public int loginUserPhoneService(String phone, String password) {
        User res = userMapper.selectByPhoneNumber(phone);
        if (res == null) {
            return -1;
        }
        if (res.getPassword().equals(password)) {
            return res.getUserId();
        }
        return -2;
    }

    @Override
    public int getUserInfoByPhone(String phone) {
        PhoneNumber res = phoneNumber.selectByPrimaryKey(phone);
        if (res == null) {
            // 0未注册
            return 0;
        } else if (!res.getIsRegistered()) {
            // 3 未注册但有记录用户
            return -3;
        } else if (!res.getIsPhoneVerified()) {
            // 1 已注册未验证
            return -1;
        } else {
            // >0 已注册已验证
            return res.getUserId();
        }
    }

    @Override
    public int getTransactionUserInfoByPhone(String phone) {
        PhoneNumber res = phoneNumber.selectByPrimaryKey(phone);
        if (res == null) {
            // 0未注册
            return 0;
        } else if (!res.getIsRegistered()) {
            // 3 未注册但有记录用户
            return -3;
        } else {
            // >0 已注册已验证
            return res.getUserId();
        }
    }

    @Override
    public int getUserInfoByEmail(String email) {
        Email res = emailMapper.selectByEmailAddress(email);
        if (res == null) {
            // 0未注册
            return 0;
        } else if (!res.getIsRegistered()) {
            // 3 未注册但有记录用户
            return -3;
        } else if (!res.getIsEmailVerified()) {
            // 1 已注册未验证
            return -1;
        } else {
            // 2 已注册已验证
            return res.getUserId();
        }
    }

    @Override
    public Email getTransactionUserInfoByEmail(String email) {
        Email res = emailMapper.selectByEmailAddress(email);
        return res;
    }

    @Override
    public int getEmailIdByEmail(String email) {
        Email res = emailMapper.selectByEmailAddress(email);
        if(res==null)
            return 0;
        return res.getEmailId();
    }

    @Override
    public String getEmailByEmailId(Integer emailId){
        Email res = emailMapper.selectByPrimaryKey(emailId);
        if(res==null)
            return null;
        return res.getEmailAddress();
    }

    @Override
    public int[] getEmailIdByUserId(Integer user_id){
        List<Email> emails = emailMapper.selectByUserId(user_id);
        if(emails.isEmpty())
            return null;
        int[] res = new int[emails.size()];
        for(int i=0;i<emails.size();i++){
            res[i] = emails.get(i).getEmailId();
        }
        return res;
    }

    @Override
    public int registerUserService(String name, String ssn, String password, BigDecimal balance) {
        User u = new User();
        u.setName(name);
        u.setSsn(ssn);
        u.setPassword(password);
        u.setBalance(new BigDecimal(0));
        // 由于递增操作，返回的是userid
        int i = userMapper.insert(u);
        return i;
    }

    @Override
    public int insertPhoneService(String phone, Integer resultCode, boolean b, boolean phone_verfied) {
        PhoneNumber p = new PhoneNumber();
        p.setPhoneNumber(phone);
        p.setIsPhoneVerified(phone_verfied);
        p.setIsRegistered(b);
        p.setUserId(resultCode);
        return phoneNumber.insert(p);
    }

    @Override
    public int insertEmailService(Integer resultCode, String emailAddress, boolean b, boolean email_verfied) {
        Email e = new Email();
        e.setEmailAddress(emailAddress);
        e.setUserId(resultCode);
        e.setIsEmailVerified(email_verfied);
        e.setIsRegistered(b);
        int a = emailMapper.insert(e);
        return a==0?0:e.getEmailId();
    }

    @Override
    public int findBySSN(String ssn) {
        Integer res = userMapper.findBySSN(ssn);
        if(res==null)
            return 0;
        return userMapper.findBySSN(ssn);
    }

    @Override
    public int updateUserInfoService(Integer user_id, String name, String password) {
        User u = new User();
        u.setUserId(user_id);
        u.setName(name);
        u.setPassword(password);
        return userMapper.updateByPrimaryKeySelective(u);
    }

    @Override
    public User findUserPasswordService(Integer user_id) {
        return userMapper.selectByPrimaryKey(user_id);
    }

    @Override
    public int updateEmailService(Integer user_id, String email, boolean v, boolean r) {
        Email e = new Email();
        e.setEmailAddress(email);
        e.setUserId(user_id);
        e.setIsEmailVerified(v);
        e.setIsRegistered(r);
        int res = emailMapper.updateEmailUser(e);
        return res;
    }

    @Override
    public int deleteEmailService(Integer user_id, String email) {
        Email e = new Email();
        e.setEmailAddress(email);
        e.setUserId(user_id);
        return emailMapper.deleteByEmailAddr(e);
    }

    @Override
    public int updatePhoneService(Integer user_id, String phoneNumber1, boolean v, boolean r){
        PhoneNumber p = new PhoneNumber();
        p.setUserId(user_id);
        p.setPhoneNumber(phoneNumber1);
        p.setIsPhoneVerified(v);
        p.setIsRegistered(r);
        return phoneNumber.updatePhoneUser(p);
    }

    @Override
    public int deletePhoneService(Integer user_id, String phoneNumber1) {
        PhoneNumber p = new PhoneNumber();
        p.setUserId(user_id);
        p.setPhoneNumber(phoneNumber1);
        return phoneNumber.deleteByPhoneNumber(p);
    }

    @Override
    public int insertBankAccountService(String bankId, String accountNumber, Boolean isJoint) {
        BankAccount u = bankAccountMapper.selectByAccountNum(accountNumber);
        if (u != null && !u.getIsJoint()) {
            // 表示非联合账户想再添加一个电子钱包
            return -1;
        } else if(u == null){
            BankAccount b = new BankAccount();
            b.setBankId(bankId);
            b.setAccountNumber(accountNumber);
            b.setIsJoint(isJoint);
            if (bankAccountMapper.insert(b) == 1) {
                u = bankAccountMapper.selectByAccountNum(accountNumber);
                if (u != null) {
                    // 表示成攻插入了一个联合账户
                    return u.getAccountId();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }else
        {
            return u.getAccountId();
        }
    }

    @Override
    public int insertUserBankAccountService(Integer userId, Integer bankAccountId, Boolean isPrimary, Boolean isVerified) {
        List<Integer> account_list = userBankAccountMapper.searchUserAccount(userId);
        if (account_list.contains(bankAccountId)) {
            // 表示已经有这个账户了
            return -1;
        }
        //首先查看是不是主资金账户
        if (isPrimary) {
            UserBankAccount u = new UserBankAccount();
            u.setUserId(userId);
            u.setAccountId(bankAccountId);
            u.setIsPrimary(isPrimary);
            u.setIsVerified(isVerified);
            if (account_list.size() != 0) {
                // 其他账户改成非主账户
                int res = userBankAccountMapper.changePrimaryAccount(u);
                if(res==0)
                {
                    // 改变主账户报错
                    return -2;
                }
            }
            int res = userBankAccountMapper.insert(u);
            if (res == 1) {
                int account_id = userBankAccountMapper.selectByUserAccount(u);
                if (account_id > 0) {
                    return account_id;
                } else {
                    return 0;
                }
            }
            return 0;
        } else {
            // 如果是第一个账户，默认主账户
            if (account_list.size() == 0) {
                isPrimary = true;
            }
            UserBankAccount u = new UserBankAccount();
            u.setUserId(userId);
            u.setAccountId(bankAccountId);
            u.setIsPrimary(isPrimary);
            u.setIsVerified(isVerified);
            int res = userBankAccountMapper.insert(u);
            if (res == 1) {
                int account_id = userBankAccountMapper.selectByUserAccount(u);
                if (account_id > 0) {
                    return account_id;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }

    @Override
    public int deleteUserBankAccountService(Integer userId, Integer bankAccountId) {
        List<Integer> account_list = userBankAccountMapper.searchUserAccount(userId);
        // 记录是否是主账户
        boolean isPrimary;
        if (!account_list.contains(bankAccountId)) {
            // 表示没有这个用户账户
            return -1;
        }
        // 查看是否是主账户
        UserBankAccount u = new UserBankAccount();
        u.setUserId(userId);
        u.setAccountId(bankAccountId);
        isPrimary = userBankAccountMapper.isPrimaryAccount(u);
        int res = userBankAccountMapper.deleteByUserAccount(u);
        if(res==0)
        {
            // 删除失败
            return -2;
        }else
        {
            // 如果删除的是主账户
            if(isPrimary)
            {
                // 还有其他账户
                if(account_list.size()>1)
                {
                    account_list.remove(bankAccountId);
                    UserBankAccount u1 = new UserBankAccount();
                    u1.setUserId(userId);
                    u1.setAccountId(account_list.get(0));
                    u1.setIsPrimary(true);
                    int res1 = userBankAccountMapper.changePrimaryAccount(u1);
                    if(res1==0)
                    {
                        // 设置主账户失败
                        return -4;
                    }
                }
            }
            // 查看该账户对应的银行卡是否还有别的用户，没有就删除
            List<Integer> user_list = userBankAccountMapper.searchAccountUser(bankAccountId);
            if(user_list.size()>0) {
                // 表示该银行账户还有别人在使用
                return 0;
            }else{
                // 表示该银行账户没有人在使用
                int res1 = bankAccountMapper.deleteByPrimaryKey(bankAccountId);
                if(res1==0)
                {
                    // 删除银行账户记录失败
                    return -3;
                }
                return 0;
            }
        }
    }

    @Override
    public List<Integer> getAccountByUserId(Integer userId) {
        return userBankAccountMapper.searchUserAccount(userId);
    }

    @Override
    public List<AccountAll> getAccountAllInfoByUserId(Integer userId) {
        return userBankAccountMapper.searchAllAccountInfo(userId);
    }

    @Override
    public List<PersonalAll> getPersonalInfoByUserId(Integer userId) {
        return userMapper.searchAllPersonalInfo(userId);
    }

    @Override
    public int changePrimaryAccountService(Integer userId, Integer account_id) {
        List<Integer> account_list = userBankAccountMapper.searchUserAccount(userId);
        // 记录是否是主账户
        boolean isPrimary;
        if (!account_list.contains(account_id)) {
            // 表示没有这个用户账户
            return -1;
        }
        else if(account_list.size()==1)
        {
            // 表示只有一个账户，已经为主账户
            return -2;
        }else{
            UserBankAccount u1 = new UserBankAccount();
            u1.setUserId(userId);
            u1.setAccountId(account_id);
            int res1 = userBankAccountMapper.changePrimaryAccount(u1);
            if(res1==0)
            {
                // 设置主账户失败
                return -3;
            }
            return 0;
        }
    }

    @Override
    public BigDecimal getBalanceByUserId(Integer userId) {
        return userMapper.selectByPrimaryKey(userId).getBalance();
    }

    @Override
    public String getPhoneByUserId(Integer userId) {
        PhoneNumber res = phoneNumber.selectPhoneByUser(userId);
        if(res==null) return null;
        return res.getPhoneNumber();
    }

    @Override
    public String getNameByUserId(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null) return null;
        return user.getName();
    }

    @Override
    public int updateBalanceByUserId(Integer userId, BigDecimal balance) {
        User u = new User();
        u.setUserId(userId);
        u.setBalance(balance);
        return userMapper.updateByPrimaryKeySelective(u);
    }

    @Override
    public List<TransactionWithBLOBs> getForePhoneBalance(String phone) {
        List<TransactionWithBLOBs> a = transactionMapper.selectByPhone(phone);
        // 返回a中所有finish时间为空的TransactionWithBLOBs
        List<TransactionWithBLOBs> b = new ArrayList<>();
        for(TransactionWithBLOBs t:a)
        {
            if(t.getTransctionFinishedTime()==null && !t.getIsCancelled())
            {
                b.add(t);
            }
        }
        return b;
    }

    @Override
    public List<TransactionWithBLOBs> getForeEmailBalance(Integer emailID) {
        List<TransactionWithBLOBs> a = transactionMapper.selectByEmailId(emailID);
        // 返回a中所有finish时间为空的TransactionWithBLOBs
        List<TransactionWithBLOBs> b = new ArrayList<>();
        for(TransactionWithBLOBs t:a)
        {
            if(t.getTransctionFinishedTime()==null && !t.getIsCancelled())
            {
                b.add(t);
            }
        }
        return b;
    }

    @Override
    public int deleteEmailRecordService(int deleteEmail, String emailAddress) {
        DeleteEmail d = new DeleteEmail();
        d.setEmailAddress(emailAddress);
        d.setEmailId(deleteEmail);
        return deleteEmailMapper.insert(d);
    }

}
