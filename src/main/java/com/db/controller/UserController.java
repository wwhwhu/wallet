package com.db.controller;

import com.db.entity.Transaction;
import com.db.entity.TransactionWithBLOBs;
import com.db.entity.User;
import com.db.service.AccountOperationService;
import com.db.util.AccountAll;
import com.db.util.PersonalAll;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.db.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class UserController {

    private final UserInfoService userService;
    private final AccountOperationService accountOptService;

    @Autowired
    public UserController(UserInfoService userService, AccountOperationService accountOptService) {
        this.userService = userService;
        this.accountOptService = accountOptService;
    }

    // 通过邮箱登录
    @RequestMapping("loginByEmail")
    public ResponseEntity<String> loginByEmail(@RequestBody UserLoginRequest request, HttpSession session) {
        String email = request.getInfo();
        String password = request.getPassword();
        // 调用 UserService 进行登录验证
        int resultCode = userService.loginUserEmailService(email, password);
        // 构建 JSON 响应
        String jsonResponse;
        if (resultCode > 0) {
            jsonResponse = "{\"status\":0,\"data\":" + resultCode + ",\"message\":\"邮箱成功登录，用户id:" + resultCode + "\"}";
            session.setAttribute("userId", resultCode);
        } else if (resultCode == -2) {
            jsonResponse = "{\"status\":1,\"message\":\"邮箱地址或密码错误\"}";
        } else if (resultCode == -1) {
            jsonResponse = "{\"status\":1,\"message\":\"未注册邮箱\"}";
        } else {
            jsonResponse = "{\"status\":1,\"message\":\"未知错误\"}";
        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 通过手机号登录
    @RequestMapping("loginByPhone")
    public ResponseEntity<String> loginByPhone(@RequestBody UserLoginRequest request, HttpSession session) {
        String phone = request.getInfo();
        String password = request.getPassword();
        // 调用 UserService 进行登录验证
        int resultCode = userService.loginUserPhoneService(phone, password);
        // 构建 JSON 响应
        String jsonResponse;
        if (resultCode > 0) {
            jsonResponse = "{\"status\":0,\"data\":" + resultCode + ",\"message\":\"手机号成功登录，用户id:" + resultCode + "\"}";
            session.setAttribute("userId", resultCode);
        } else if (resultCode == -2) {
            jsonResponse = "{\"status\":1,\"message\":\"手机号或密码错误\"}";
        } else if (resultCode == -1) {
            jsonResponse = "{\"status\":1,\"message\":\"未注册手机号\"}";
        } else {
            jsonResponse = "{\"status\":1,\"message\":\"未知错误\"}";
        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 注册
    @RequestMapping("register")
    public ResponseEntity<String> register(@RequestBody JsonNode jsonNode) throws JsonProcessingException {
        // 在这里处理接收到的 JSON 数据
        String name = jsonNode.get("name").asText();
        String ssn = jsonNode.get("ssn").asText();
        String password = jsonNode.get("password").asText();
        String emailAddress = jsonNode.get("email_address").asText();
        String phone = jsonNode.get("phone").asText();
        String is_phone_verfied = jsonNode.get("is_phone_verified").asText();
        String is_email_verfied = jsonNode.get("is_email_verified").asText();
        String jsonResponse;
        if (Objects.equals(name, "") || Objects.equals(ssn, "") || Objects.equals(password, "") || Objects.equals(emailAddress, "") || Objects.equals(phone, "") || Objects.equals(is_phone_verfied, "") || Objects.equals(is_email_verfied, "")) {
            jsonResponse = "{\"status\":1,\"message\":\"空字段非法\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        boolean phone_verfied = Boolean.parseBoolean(is_phone_verfied);
        boolean email_verfied = Boolean.parseBoolean(is_email_verfied);
        int resultCode = 0;
        int resultCode2 = 0;
        int resultCode3 = 0;
        // 首先根据phone判断是否存在手机号已注册已验证用户，已经存在则无需注册
        int isLogin = userService.getUserInfoByPhone(phone);
        if (isLogin > 0) {
            // 已注册用户
            jsonResponse = "{\"status\":1,\"message\":\"手机号已经注册，请登录完善信息\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        } else if (isLogin == -1) {
            // 已注册未验证用户
            jsonResponse = "{\"status\":1,\"message\":\"手机号已经注册，但未验证，请登录验证\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }

        // 再根据email判断是否存在Email已注册已验证用户，已经存在则无需注册
        int isLogin2 = userService.getUserInfoByEmail(emailAddress);
        if (isLogin2 > 0) {
            // 已注册用户
            jsonResponse = "{\"status\":1,\"message\":\"邮箱已经注册，请登录完善信息\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        } else if (isLogin2 == -1) {
            // 已注册未验证用户
            jsonResponse = "{\"status\":1,\"message\":\"邮箱已经注册，但未验证，请登录验证\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }

        // 手机号邮箱不存在
        // 调用 UserService 进行注册
        // 返回UserID

        try {
            resultCode = userService.registerUserService(name, ssn, password, new BigDecimal(0));
        } catch (Exception e) {
            // 插入错误
            jsonResponse = "{\"status\":" + 1 + ",\"message\":\"SSN重复\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }

        if (resultCode == 0) {
            // 插入错误
            jsonResponse = "{\"status\":" + 1 + ",\"message\":\"注册错误\"}";
        } else {
            int user_id = userService.findBySSN(ssn);
            if (isLogin == -3) {
                // Update即可
                resultCode2 = userService.updatePhoneService(user_id, phone, true, phone_verfied);
            }
            if (isLogin2 == -3) {
                // Update即可
                resultCode3 = userService.updateEmailService(user_id, emailAddress, true, phone_verfied);
            }
            if (isLogin == 0) {
                resultCode2 = userService.insertPhoneService(phone, user_id, true, phone_verfied);
            }
            if (isLogin2 == 0){
                resultCode3 = userService.insertEmailService(user_id, emailAddress, true, email_verfied);
            }
            if (resultCode2 == 0) {
                // 插入错误
                jsonResponse = "{\"status\":" + 1 + ",\"message\":\"手机号注册错误\"}";
            } else if (resultCode3 == 0) {
                // 插入错误
                jsonResponse = "{\"status\":" + 1 + ",\"message\":\"邮箱注册错误\"}";
            }
            // 注册成功
            else {
                resultCode3 = userService.getEmailIdByEmail(emailAddress);
                // 在Tranction表中查找，没有完成的交易
                List<TransactionWithBLOBs> a = userService.getForePhoneBalance(phone);
                List<TransactionWithBLOBs> b = userService.getForeEmailBalance(resultCode3);
                // 计算a与b中所有balance之和，并对每个Transacion的完成时间进行update
                BigDecimal balance = new BigDecimal(0);
                int i = 0;
                for (TransactionWithBLOBs transactionWithBLOBs : a) {
                    if(!transactionWithBLOBs.getIsCancelled()) {
                        balance = balance.add(transactionWithBLOBs.getAmount());
                        accountOptService.recordEndTimeTransactionService(transactionWithBLOBs.getTransactionId());
                        accountOptService.updateUserIndo(transactionWithBLOBs.getTransactionId(), user_id);
                        i++;
                    }
                }
                for (TransactionWithBLOBs transactionWithBLOBs : b) {
                    if(!transactionWithBLOBs.getIsCancelled()) {
                        balance = balance.add(transactionWithBLOBs.getAmount());
                        accountOptService.recordEndTimeTransactionService(transactionWithBLOBs.getTransactionId());
                        accountOptService.updateUserIndo(transactionWithBLOBs.getTransactionId(), user_id);
                        i++;
                    }
                }
                // 合并a,b
                a.addAll(b);
                // 更新balance
                userService.updateBalanceByUserId(user_id, balance);
                jsonResponse = "{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(a) + ",\"message\":\"注册成功！你的用户id是:" + user_id + "，你有一共"+i+"笔的共计"+balance+"的汇款\"}";
            }

        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 这里假设有一个用于接收登录请求的请求体类
    static class UserLoginRequest {
        private String info;
        private String password;

        public String getInfo() {
            return info;
        }

        public String getPassword() {
            return password;
        }

        public void setEmail(String info) {
            this.info = info;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // 更新User信息
    @RequestMapping("updateUserInfo")
    public ResponseEntity<String> register(@RequestBody JsonNode jsonNode, HttpSession session) {
//        String user_id = session.getAttribute("userId").toString(); // 获取用户id
        Integer user_id0 = Integer.parseInt(jsonNode.get("user_id").asText());
        String name = jsonNode.get("name").asText();
        String oldPassword = jsonNode.get("oldPassword").asText();
        User nowUser = userService.findUserPasswordService(user_id0);
        String realPassword = nowUser.getPassword();
        String oldName = nowUser.getName();
        if (!oldPassword.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"原密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String newPassword;
        // 保持原密码不变，只改变name
        if (jsonNode.get("newPassword") == null || jsonNode.get("newPassword").asText().equals("")) {
            newPassword = realPassword;
        } else
        {
            newPassword = jsonNode.get("newPassword").asText();
//            if (newPassword.equals(oldPassword)) {
//                String jsonResponse = "{\"status\":1,\"message\":\"新密码与原密码相同\"}";
//                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//            }
        }
        if (name == null || name.equals("")) {
            name = oldName;
        }
//      else if (name.equals(oldName)) {
//            String jsonResponse = "{\"status\":1,\"message\":\"未更新用户信息\"}";
//            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//        }
//        if(user_id == null)
//        {
//            String jsonResponse = "{\"status\":1,\"message\":\"用户未登录\"}";
//            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//        }
//        else if(user.getUserId()!=Integer.parseInt(user_id))
//        {
//            String jsonResponse = "{\"status\":1,\"message\":\"登录用户id不匹配\"}";
//            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//        }
//        else
//        {
        int resultCode = userService.updateUserInfoService(user_id0, name, newPassword);
        String jsonResponse;
        if (resultCode == 0) {
            // 更新
            jsonResponse = "{\"status\":" + 1 + ",\"message\":\"更新错误\"}";
        } else {
            jsonResponse = "{\"status\":0,\"data\":" + user_id0 + ",\"message\":\"更新成功！你的用户id是:" + user_id0 + "\"}";
        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//        }
    }

    // 增删Email
    @RequestMapping("updateEmailInfo")
    public ResponseEntity<String> updateEmailInfo(@RequestBody JsonNode jsonNode, HttpSession session) {
        String user_id = jsonNode.get("user_id").asText();
        String emailAddress = jsonNode.get("email_address").asText();
        String Password = jsonNode.get("password").asText();
        String isAddEmail = jsonNode.get("isAddEmail").asText();
        String is_email_verfied = jsonNode.get("is_email_verified").asText();
        String is_email_registered = jsonNode.get("is_email_registered").asText();
        if (Objects.equals(user_id, "") || Objects.equals(Password, "") || Objects.equals(emailAddress, "") || Objects.equals(isAddEmail, "") || Objects.equals(is_email_verfied, "") || Objects.equals(is_email_registered, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        boolean is_Add = Boolean.parseBoolean(isAddEmail);
        boolean email_verfied = Boolean.parseBoolean(is_email_verfied);
        boolean email_registered = Boolean.parseBoolean(is_email_registered);
        if (!(email_verfied && email_registered)) {
            String jsonResponse = "{\"status\":1,\"message\":\"请先验证\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!Password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String jsonResponse;
        if (is_Add) {
            int isLogin2 = userService.getUserInfoByEmail(emailAddress);
            if (isLogin2 > 0 || isLogin2 == -1) {
                // 已注册用户
                jsonResponse = "{\"status\":1,\"message\":\"邮箱已经被绑定，无法继续绑定\"}";
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            } else if (isLogin2 == -3) {
                // 未注册但有记录用户，添加Update User_id
                int res = userService.updateEmailService(Integer.parseInt(user_id), emailAddress, email_verfied, email_registered);
                if (res == 0) {
                    jsonResponse = "{\"status\":1,\"message\":\"未注册但有记录，更新绑定用户信息时错误\"}";
                } else {
                    jsonResponse = "{\"status\":0,\"data\":" + emailAddress + ",\"message\":\"未注册但有记录，更新绑定用户信息时成功\"}";
                }
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            } else {
                // 增加
                int resultCode = userService.insertEmailService(Integer.parseInt(user_id), emailAddress, email_registered, email_verfied);
                if (resultCode == 0) {
                    // 更新
                    jsonResponse = "{\"status\":" + 1 + ",\"message\":\"添加邮箱错误\"}";
                } else {
                    jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"添加用户" + user_id + "的邮箱" + emailAddress + "成功\"}";
                }
                // 返回 ResponseEntity 对象，设置状态码和响应体
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            }
        } else {
            // 删除
            int deleteEmail = userService.getEmailIdByEmail(emailAddress);
            if(deleteEmail == 0)
            {
                jsonResponse = "{\"status\":1,\"message\":\"邮箱不存在\"}";
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            }
            int resultCode = userService.deleteEmailService(Integer.parseInt(user_id), emailAddress);

            if (resultCode == 0) {
                // 更新
                jsonResponse = "{\"status\":" + 1 + ",\"message\":\"邮箱删除错误，不存在相关记录\"}";
            } else {
                int recordDelete = userService.deleteEmailRecordService(deleteEmail, emailAddress);
                if(recordDelete == 0)
                {
                    jsonResponse = "{\"status\":1,\"message\":\"邮箱删除记录错误\"}";
                    return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
                }
                jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"删除用户" + user_id + "的邮箱" + emailAddress + "成功\"}";
            }
            // 返回 ResponseEntity 对象，设置状态码和响应体
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
    }

    // 增删Phone
    @RequestMapping("updatePhoneInfo")
    public ResponseEntity<String> updatePhoneInfo(@RequestBody JsonNode jsonNode, HttpSession session) {
        String user_id = jsonNode.get("user_id").asText();
        String phoneNumber = jsonNode.get("phone_number").asText();
        String password = jsonNode.get("password").asText();
        String isAddPhone = jsonNode.get("isAddPhone").asText();
        String is_phone_verfied = jsonNode.get("is_phone_verified").asText();
        String is_phone_registered = jsonNode.get("is_phone_registered").asText();
        if (Objects.equals(user_id, "") || Objects.equals(password, "") || Objects.equals(phoneNumber, "") || Objects.equals(isAddPhone, "") || Objects.equals(is_phone_verfied, "") || Objects.equals(is_phone_registered, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        boolean is_Add = Boolean.parseBoolean(isAddPhone);
        boolean phone_verfied = Boolean.parseBoolean(is_phone_verfied);
        boolean phone_registered = Boolean.parseBoolean(is_phone_registered);
        if (!(phone_verfied && phone_registered)) {
            String jsonResponse = "{\"status\":1,\"message\":\"请先验证\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String jsonResponse;
        if (is_Add) {
            int isLogin2 = userService.getUserInfoByPhone(phoneNumber);
            if (isLogin2 > 0 || isLogin2 == -1) {
                // 已注册用户
                jsonResponse = "{\"status\":1,\"message\":\"手机已经被绑定，无法继续绑定\"}";
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            } else if (isLogin2 == -3) {
                // 未注册但有记录用户，添加Update User_id
                int res = userService.updatePhoneService(Integer.parseInt(user_id), phoneNumber, phone_verfied, phone_registered);
                if (res == 0) {
                    jsonResponse = "{\"status\":1,\"message\":\"未注册但有记录，更新绑定用户信息时错误\"}";
                } else {
                    jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"未注册但有记录，更新绑定用户信息时成功\"}";
                }
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            } else {
                // 判断该用户是否已经有手机绑定（修改了PhoneNumberMapper，方法返回的是手机号码而不是0和1）
                if(userService.getPhoneByUserId(Integer.parseInt(user_id))!=null){
                    jsonResponse = "{\"status\":1,\"message\":\"该用户已经有手机绑定，无法继续绑定\"}";
                    return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
                }
                // 增加
                int resultCode = userService.insertPhoneService(phoneNumber, Integer.parseInt(user_id), phone_registered, phone_verfied);
                if (resultCode == 0) {
                    // 更新
                    jsonResponse = "{\"status\":" + 1 + ",\"message\":\"添加手机错误\"}";
                } else {
                    jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"添加用户" + user_id + "的手机" + phoneNumber + "成功\"}";
                }
                // 返回 ResponseEntity 对象，设置状态码和响应体
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            }
        } else {
            // 删除
            int resultCode = userService.deletePhoneService(Integer.parseInt(user_id), phoneNumber);
            if (resultCode == 0) {
                // 更新
                jsonResponse = "{\"status\":" + 1 + ",\"message\":\"手机删除错误，不存在相关记录\"}";
            } else {
                jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"删除用户" + user_id + "的手机" + phoneNumber + "成功\"}";
            }
            // 返回 ResponseEntity 对象，设置状态码和响应体
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
    }

    // 根据用户id获取所有账户信息
    @RequestMapping("getAccountByUserId")
    public ResponseEntity<String> getAccountAllInfoByUserId(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        String user_id = jsonNode.get("user_id").asText();
        if (Objects.equals(user_id, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<AccountAll> accountList = userService.getAccountAllInfoByUserId(Integer.parseInt(user_id));
        String jsonResponse;
        if (accountList.size() == 0) {
            jsonResponse = "{\"status\":1,\"message\":\"用户账户不存在\"}";
        } else {
            jsonResponse = "{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(accountList) + ",\"message\":\"获取用户" + user_id + "的所有account_id成功\"}";
        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 增加银行账户并绑定
    @RequestMapping("addBankInfo")
    public ResponseEntity<String> updateBankInfo(@RequestBody JsonNode jsonNode, HttpSession session) {
        String user_id = jsonNode.get("user_id").asText();
        String bank_id = jsonNode.get("bank_id").asText();
        String account_number = jsonNode.get("account_number").asText();
        String is_joint = jsonNode.get("is_joint").asText();
        String is_primary = jsonNode.get("is_account_primary").asText();
        String is_verified = jsonNode.get("is_account_verified").asText();
        String password = jsonNode.get("password").asText();
        if (Objects.equals(user_id, "") || Objects.equals(bank_id, "") || Objects.equals(account_number, "") || Objects.equals(is_joint, "") || Objects.equals(is_primary, "") || Objects.equals(is_verified, "") || Objects.equals(password, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        boolean is_Joint = Boolean.parseBoolean(is_joint);
        boolean is_Verfied = Boolean.parseBoolean(is_verified);
        boolean is_Primary = Boolean.parseBoolean(is_primary);
        if (!is_Verfied) {
            String jsonResponse = "{\"status\":1,\"message\":\"请先验证银行账户\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String jsonResponse;
        // 返回Account_id
        int isLogin2 = userService.insertBankAccountService(bank_id, account_number, is_Joint);
        if (isLogin2 == -1) {
            // 非联合账户
            jsonResponse = "{\"status\":1,\"message\":\"该银行账户非联合账户且被绑定，无法添加\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        } else if (isLogin2 == 0) {
            // 添加失败
            jsonResponse = "{\"status\":1,\"message\":\"未知错误，添加银行卡失败\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        } else {
            // 添加成功,绑定
            int res = userService.insertUserBankAccountService(Integer.parseInt(user_id), isLogin2, is_Primary, is_Verfied);
            if (res == 0) {
                jsonResponse = "{\"status\":1,\"message\":\"添加银行卡成功，但绑定失败\"}";
            } else if (res == -1) {
                jsonResponse = "{\"status\":1,\"message\":\"添加银行卡成功，但绑定失败，该银行卡已被绑定\"}";
            } else if (res == -2) {
                jsonResponse = "{\"status\":1,\"message\":\"添加银行卡成功，但绑定设置为主账户时失败\"}";
            } else {
                jsonResponse = "{\"status\":0,\"data\":" + res + ",\"message\":\"成功添加银行卡并绑定，user_account_id:" + res + "\"}";
            }
        }
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 删除银行账户
    @RequestMapping("removeBankInfo")
    public ResponseEntity<String> removeBankInfo(@RequestBody JsonNode jsonNode, HttpSession session) {
        String user_id = jsonNode.get("user_id").asText();
        String account_id = jsonNode.get("account_id").asText();
        String password = jsonNode.get("password").asText();
        if (Objects.equals(user_id, "") || Objects.equals(account_id, "") || Objects.equals(password, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String jsonResponse;
        // 删除一个UserBankaccount
        int resultCode = userService.deleteUserBankAccountService(Integer.parseInt(user_id), Integer.parseInt(account_id));
        if (resultCode == -1) {
            jsonResponse = "{\"status\":1,\"message\":\"删除用户账户失败，不存在相关记录\"}";
        } else if (resultCode == -2) {
            jsonResponse = "{\"status\":1,\"message\":\"删除用户账户失败，未知错误\"}";
        } else if (resultCode == -3) {
            jsonResponse = "{\"status\":1,\"message\":\"删除用户账户成功，但消除银行账户记录失败\"}";
        } else if (resultCode == -4) {
            jsonResponse = "{\"status\":1,\"message\":\"删除用户账户成功，但该账户是主账户，重新设置别的主账户失败\"}";
        } else {
            jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"删除用户" + user_id + "的账户" + account_id + "成功\"}";
        }
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 根据用户id获取所有个人隐私信息
    @RequestMapping("getPersonalInfoByUserId")
    public ResponseEntity<String> getUserAllInfoByUserId(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        String user_id = jsonNode.get("user_id").asText();
        if (Objects.equals(user_id, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<PersonalAll> personalList = userService.getPersonalInfoByUserId(Integer.parseInt(user_id));
        String jsonResponse;
        if (personalList.size() == 0) {
            jsonResponse = "{\"status\":1,\"message\":\"用户账户不存在\"}";
        } else {
            jsonResponse = "{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(personalList) + ",\"message\":\"获取用户" + user_id + "的所有个人信息成功\"}";
        }
        // 返回 ResponseEntity 对象，设置状态码和响应体
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    // 改变主账户
    @RequestMapping("changePrimaryAccount")
    public ResponseEntity<String> changePrimaryAccount(@RequestBody JsonNode jsonNode, HttpSession session) {
        String user_id = jsonNode.get("user_id").asText();
        String account_id = jsonNode.get("primary_account_id").asText();
        String password = jsonNode.get("password").asText();
        if (Objects.equals(user_id, "") || Objects.equals(account_id, "") || Objects.equals(password, "")) {
            String jsonResponse = "{\"status\":1,\"message\":\"非法空字段\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        String jsonResponse;
        int resultCode = userService.changePrimaryAccountService(Integer.parseInt(user_id), Integer.parseInt(account_id));
        if (resultCode == -1) {
            jsonResponse = "{\"status\":1,\"message\":\"更改主账户失败，不存在该主账户\"}";
        } else if (resultCode == -2) {
            jsonResponse = "{\"status\":1,\"message\":\"只有这一个账户，默认为主账户无需更改\"}";
        } else if (resultCode == -3) {
            jsonResponse = "{\"status\":1,\"message\":\"设置主账户失败，未知错误\"}";
        } else {
            jsonResponse = "{\"status\":0,\"data\":" + user_id + ",\"message\":\"修改用户" + user_id + "主账户为" + account_id + "成功\"}";
        }
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

}
