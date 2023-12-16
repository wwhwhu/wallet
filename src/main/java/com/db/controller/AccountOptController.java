package com.db.controller;

import com.db.entity.*;
import com.db.service.AccountOperationService;
import com.db.service.UserInfoService;
import com.db.util.BestSeller;
import com.db.util.MonthStatistics;
import com.db.util.Transinfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RestController
@CrossOrigin
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class AccountOptController {
    private final AccountOperationService accountOptService;
    private final UserInfoService userService;

    @Autowired
    public AccountOptController(AccountOperationService accountOptService, UserInfoService userService) {
        this.accountOptService = accountOptService;
        this.userService = userService;
    }

    @RequestMapping("/transaction")
    public ResponseEntity<String> transaction(@RequestBody JsonNode jsonNode, HttpSession session) {
        String send_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String email_id = jsonNode.get("email_address") == null ? null : jsonNode.get("email_address").asText();
        String phone_number = jsonNode.get("phone_number") == null ? null : jsonNode.get("phone_number").asText();
        String amount = jsonNode.get("amount").asText();
        String meno = jsonNode.get("memo").asText();
        TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
        TimeZone.setDefault(time);
        Date startTime = new Date();
        // 使用电子钱包还是银行卡
        String fun = jsonNode.get("isPayByWallet").asText();
        if (send_id == null || send_id.equals("") || password == null || password.equals("") || amount == null || amount.equals("") || fun == null || fun.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        // 转化fun为boolean
        boolean isPayByWallet = Boolean.parseBoolean(fun);
        if ((email_id == null || email_id.equals("")) && (phone_number == null || phone_number.equals(""))) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(send_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        if (isPayByWallet) {
            BigDecimal walletmoney = nowUser.getBalance();
            if (walletmoney.compareTo(BigDecimal.valueOf(Double.parseDouble(amount))) < 0) {
                return ResponseEntity.ok("{\"status\":1,\"message\":\"电子钱包余额不足\"}");
            }
        }
        // 按照手机号转账
        if (email_id == null || email_id.equals("")) {
            int res0 = userService.getTransactionUserInfoByPhone(phone_number);
            if (res0 == 0 || res0 == -3) {
                // 向无账户者转账
                // 首先增加一个phone记录
                if (res0 == 0) {
                    int res = userService.insertPhoneService(phone_number, null, false, false);
                    if (res == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"插入手机号错误\"}");
                    }
                }
                // 然后增加一个Transaction记录
                int res2 = accountOptService.insertTransactionService(Integer.parseInt(send_id), null, Double.parseDouble(amount), meno, null, phone_number, startTime);
                if (res2 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"向新账户转账失败\"}");
                }
                if (isPayByWallet) {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id), newwalletmoney);
                    if (res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 成功向无账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":" + res2 + ",\"message\":\"向新账户转账成功\"}");
            } else {
                if (Integer.parseInt(send_id) == res0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"不能向自己转账\"}");
                }
                // 向有账户者转账
                int res = accountOptService.insertTransactionService(Integer.parseInt(send_id), res0, Double.parseDouble(amount), meno, null, phone_number, startTime);
                if (res == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"已有账户转账失败\"}");
                }
                if (isPayByWallet) {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id), newwalletmoney);
                    if (res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 增加他人余额
                BigDecimal othermoney = userService.findUserPasswordService(res0).getBalance();
                BigDecimal newothermoney = othermoney.add(BigDecimal.valueOf(Double.parseDouble(amount)));
                int res4 = userService.updateBalanceByUserId(res0, newothermoney);
                if (res4 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"更新他人余额失败\"}");
                }
                accountOptService.recordEndTimeTransactionService(res);
                // 成功向有账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":" + res + ",\"message\":\"已有账户转账成功\"}");
            }
        }
        // 按照Email转账
        if (phone_number == null || phone_number.equals("")) {
            Email res0 = userService.getTransactionUserInfoByEmail(email_id);
            if (res0 == null || !res0.getIsRegistered()) {
                // 向无账户者转账
                // 首先增加一个Email记录
                int email_id0;
                if (res0 == null) {
                    int res = userService.insertEmailService(null, email_id, false, false);
                    if (res == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"插入邮箱错误\"}");
                    }
                    email_id0 = userService.getTransactionUserInfoByEmail(email_id).getEmailId();
                } else {
                    email_id0 = res0.getEmailId();  // 有账户但未注册
                }
                // 然后增加一个Transaction记录
                int res2 = accountOptService.insertTransactionService(Integer.parseInt(send_id), null, Double.parseDouble(amount), meno, email_id0, null, startTime);
                if (res2 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"向新账户转账失败\"}");
                }
                if (isPayByWallet) {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id), newwalletmoney);
                    if (res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 成功向无账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":" + res2 + ",\"message\":\"向新账户转账成功\"}");
            } else {
                if (Integer.parseInt(send_id) == res0.getUserId()) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"不能向自己转账\"}");
                }
                // 向有账户者转账
                int res = accountOptService.insertTransactionService(Integer.parseInt(send_id), res0.getUserId(), Double.parseDouble(amount), meno, res0.getEmailId(), null, startTime);
                if (res == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"已有账户转账失败\"}");
                }
                if (isPayByWallet) {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id), newwalletmoney);
                    if (res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 增加他人余额
                BigDecimal othermoney = userService.findUserPasswordService(res0.getUserId()).getBalance();
                BigDecimal newothermoney = othermoney.add(BigDecimal.valueOf(Double.parseDouble(amount)));
                int res4 = userService.updateBalanceByUserId(res0.getUserId(), newothermoney);
                if (res4 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"更新他人余额失败\"}");
                }
                accountOptService.recordEndTimeTransactionService(res);
                // 成功向有账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":" + res + ",\"message\":\"已有账户，转账成功\"}");
            }
        } else {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"无法同时输入Email和电话号码\"}");
        }
    }

    @RequestMapping("/searchTransactionPerMonth")
    public ResponseEntity<String> searchTransactionPerMonth(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 统计以开始时间为准的一个月内的交易记录（包含已取消的）
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String year = jsonNode.get("year").asText();
        String month = jsonNode.get("month").asText();
        if (year == null || year.equals("") || month == null || month.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionPerMonthService(Integer.parseInt(user_id), Integer.parseInt(year), Integer.parseInt(month));
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            // 统计总金额与平均金额,统计最大金额与其对应的交易ID
            BigDecimal total = new BigDecimal(0);
            BigDecimal max = new BigDecimal(0);
            // 统计的时候不包括取消的交易
            int maxId = 0;
            int j = 0;
            for (int i = 0; i < res.size(); i++) {
                if (!res.get(i).getIsCancelled()) {
                    if (res.get(i).getAmount().compareTo(max) > 0) {
                        max = res.get(i).getAmount();
                        maxId = res.get(i).getTransactionId();
                    }
                    total = total.add(res.get(i).getAmount());
                    j++;
                }
            }
            // 获取所有为最高金额的交易ID
            List<Integer> maxIdList = new ArrayList<>();
            for (int i = 0; i < res.size(); i++) {
                if (res.get(i).getAmount().compareTo(max) == 0 && !res.get(i).getIsCancelled()) {
                    maxIdList.add(res.get(i).getTransactionId());
                }
            }
            List<Transinfo> rest = transtransaction(res);
            BigDecimal average = total.divide(BigDecimal.valueOf(res.size()), BigDecimal.ROUND_CEILING);
            // 将这些统计参数放到工具类中
            MonthStatistics monthStatistics = new MonthStatistics();
            monthStatistics.setYear(Integer.parseInt(year));
            monthStatistics.setMonth(Integer.parseInt(month));
            monthStatistics.setTotalAmount(total);
            monthStatistics.setAverageAmount(average);
            monthStatistics.setMaxAmount(max);
            monthStatistics.setMaxID(maxIdList);
            monthStatistics.setTotalTimes(j);
            monthStatistics.setMonthStatisticsList(rest);
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(monthStatistics) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionBySSN")
    public ResponseEntity<String> searchTransactionBySSN(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过SSN查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String ssn = jsonNode.get("ssn").asText();
        if (ssn == null || ssn.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        // 首先根据SSN查询用户ID
        int userId = userService.findBySSN(ssn);
        if (userId == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"无交易记录\"}");
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByUserIdService(Integer.parseInt(user_id), userId);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            List<Transinfo> rest = transtransaction(res);
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(rest) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionByEmail")
    public ResponseEntity<String> searchTransactionByEmail(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过Email查询交易记录（user_id：支付者，email_address：收款方）
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String email = jsonNode.get("email_address").asText();
        if (email == null || email.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByEmailService(Integer.parseInt(user_id), email);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            List<Transinfo> rest = transtransaction(res);
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(rest) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionByPhone")
    public ResponseEntity<String> searchTransactionByPhone(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过Phone查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String phone = jsonNode.get("phone").asText();
        if (phone == null || phone.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByPhoneService(Integer.parseInt(user_id), phone);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            List<Transinfo> rest = transtransaction(res);
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(rest) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionByDateRange")
    public ResponseEntity<String> searchTransactionByDateRange(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过日期范围查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String start = jsonNode.get("start").asText();
        String end = jsonNode.get("end").asText();
        // 转换start,end为Date类型
        Date startDate;
        Date endDate;
        if (start == null || start.equals("") || end == null || end.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startDate = sdf.parse(start);
            endDate = sdf.parse(end);
        } catch (ParseException e) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"日期格式错误\"}");
        }
        if (endDate.before(startDate)) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"日期范围错误\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByDateRangeService(Integer.parseInt(user_id), startDate, endDate);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            // 统计总金额
            BigDecimal total = new BigDecimal(0);
            for (TransactionWithBLOBs transaction : res) {
                if (!transaction.getIsCancelled()) {
                    total = total.add(transaction.getAmount());
                }
            }
            // 封装返回数据
            class Res {
                private List<Transinfo> data;
                private BigDecimal total;

                public List<Transinfo> getData() {
                    return data;
                }

                public void setData(List<Transinfo> data) {
                    this.data = data;
                }

                public BigDecimal getTotal() {
                    return total;
                }

                public void setTotal(BigDecimal total) {
                    this.total = total;
                }
            }
            Res res1 = new Res();
            List<Transinfo> rest = transtransaction(res);
            res1.setData(rest);
            res1.setTotal(total);
            // 同时返回Total加res,两者均在data中
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(res1) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionCancelled")
    public ResponseEntity<String> searchTransactionCancelled(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查询已取消的交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        if (user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionCancelledService(Integer.parseInt(user_id));
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            List<Transinfo> rest = transtransaction(res);
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(rest) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/CancelTransaction")
    public ResponseEntity<String> cancelTransaction(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 取消交易
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String transaction_id = jsonNode.get("transaction_id").asText();
        String reason = jsonNode.get("cancel_reason").asText();
        if (user_id == null || user_id.equals("") || password == null || password.equals("") || transaction_id == null || transaction_id.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        int res = accountOptService.cancelTransactionService(Integer.parseInt(user_id), Integer.parseInt(transaction_id), reason);
        if (res == -1) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"当前用户无该转账记录\"}");
        } else if (res == -2) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"重复取消，错误\"}");
        } else if (res == -3) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"超过10分钟无法取消\"}");
        } else if (res == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"取消失败\"}");
        } else {
            // 获取当前已取消Transaction返回
            TransactionWithBLOBs l = accountOptService.getTransactionByIdService(Integer.parseInt(transaction_id));
            Transinfo t = new Transinfo();
            t.setTransactionId(l.getTransactionId());
            t.setSenderUserId(l.getSenderUserId());
            t.setRecipientUserId(l.getRecipientUserId());
            t.setRecipientEmailId(l.getRecipientEmailId());
            t.setRecipientPhoneNumber(l.getRecipientPhoneNumber());
            t.setTransactionStartTime(l.getTransactionStartTime());
            t.setTransctionFinishedTime(l.getTransctionFinishedTime());
            t.setIsCancelled(l.getIsCancelled());
            t.setCancelledTime(l.getCancelledTime());
            t.setCancelledReason(l.getCancelledReason());
            t.setAmount(l.getAmount());
            t.setMemo(l.getMemo());
            // 获取Username
            t.setUserName(userService.getNameByUserId(l.getSenderUserId()));
            // 获取EmailAddress
            if (l.getRecipientEmailId() != null)
                t.setEmailAddress(userService.getEmailByEmailId2(l.getRecipientEmailId()));
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(t) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchBestSeller")
    public ResponseEntity<String> searchBestSeller(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查询最佳卖家
        Integer user_id = jsonNode.get("user_id").asInt();
        String password = jsonNode.get("password").asText();
        String start = jsonNode.get("start").asText();
        String end = jsonNode.get("end").asText();
        if (start == null || start.equals("") || end == null || end.equals("") || user_id == null || user_id.equals("") || password == null || password.equals("")) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        Date startDate;
        Date endDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startDate = sdf.parse(start);
            endDate = sdf.parse(end);
            if (endDate.before(startDate)) {
                return ResponseEntity.ok("{\"status\":1,\"message\":\"日期范围错误\"}");
            }
        } catch (ParseException e) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"日期格式错误\"}");
        }
        User nowUser = userService.findUserPasswordService(user_id);
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<BestSeller> res = accountOptService.searchBestSellerService(user_id, startDate, endDate);
        // List<BigDecimal> res = accountOptService.searchBestSellerService(user_id, startDate, endDate);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.isEmpty()) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            res.get(0).setName(userService.findUserPasswordService(res.get(0).getRecipientUserId()).getName());
            //return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(res.get(0)) + ",\"message\":\"查询成功\"}");
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(res.get(0)) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/requestFromGroup")
    public ResponseEntity<String> requestFromGroup(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 向一群人发起收款（单独收款是特例）
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);   // 前端发回email_address，后端是id，不能直接映射
        Integer requesterUserId = Integer.valueOf(jsonNode.get("user_id").asInt());
        BigDecimal totalAmount = BigDecimal.valueOf(jsonNode.get("total_amount").asDouble());
        String memo = jsonNode.get("memo") == null ? null : jsonNode.get("memo").asText();

        // 需要后端补足的值：日期，contributionId，requestId，transactionId，isContributed
        TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
        TimeZone.setDefault(time);
        Date requestTime = new Date();

        // 向数据库写入request
        int requestId = accountOptService.insertRequestService(requesterUserId, totalAmount, requestTime, memo);

        // 向数据库写入contributions
        //System.out.println("jsonNode.get:" + jsonNode.get("contributions").toString());
        // asText, textValue失效，使用toString；映射有效字段少，直接用循环处理
        //List<RequestContribution> contributions = objectMapper.readValue(jsonNode.get("contributions").toString(), new TypeReference<List<RequestContribution>>(){});
        List<RequestContribution> contributions = new ArrayList<>();
        for (JsonNode node : jsonNode.get("contributions")) {
            RequestContribution rc = new RequestContribution();
            String senderPhoneNumber = node.get("sender_phone_number") == null ? null : node.get("sender_phone_number").asText();
            String senderEmail = node.get("sender_email") == null ? null : node.get("sender_email").asText();
            // 查错
            int senderPhoneNumberId = 0;
            int senderEmailId = 0;
            if (senderPhoneNumber != null) senderPhoneNumberId = userService.getUserInfoByPhone(senderPhoneNumber);
            if (senderEmail != null) senderEmailId = userService.getEmailIdByEmail(senderEmail);
            // 若sender_phone_number未注册，或sender_email未注册，返回错误信息
            if (senderPhoneNumber == null && senderEmail == null)
                return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
            else if (senderPhoneNumber == null && senderEmailId == 0 || senderEmail == null && senderPhoneNumberId <= 0) {
                return ResponseEntity.ok("{\"status\":1,\"message\":\"输入的邮箱或手机号无效\"}");
            }
            rc.setSenderPhoneNumber(senderPhoneNumber);
            rc.setSenderEmailId(senderEmailId == 0 ? null : senderEmailId);
            rc.setRequestId(requestId);
            rc.setContributionAmount(BigDecimal.valueOf(node.get("contribution_amount").asDouble()));
            rc.setIsContributed(false);
            contributions.add(rc);
        }
        //System.out.println("contributions:"+contributions);
        // 可通过contributionIds获取刚刚成功发起的群收款整体情况
        int[] contributionIds = accountOptService.insertRequestContributionService(contributions);

        return ResponseEntity.ok("{\"status\":0,\"message\":\"ok\"}");
    }

    @RequestMapping("/searchGroupRequestByUserId")
    public ResponseEntity<String> searchGroupRequestByUserId(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查看我发起的所有群收款（强调发起：所以前端给的是requesterUserId）
        // 有个bug：返回的时间是按秒算的！！
        Integer requesterUserId = jsonNode.get("user_id").asInt();
        List<Request> res = accountOptService.searchGroupRequestByUserIdService(requesterUserId);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.isEmpty()) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(res) + ",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchGroupContributionByRequestId")
    public ResponseEntity<String> searchGroupContributionByRequestId(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查询指定requestId的群收款（具体到每一条contribution）
        // 前端有误：应当是返回contributions数组
        Integer requestId = jsonNode.get("request_id").asInt();
        List<RequestContribution> requestContributions = accountOptService.searchGroupContributionByRequestIdService(requestId);
        // 将有sender_email_id的，改成对应的sender_email(email_address)，需要重新封装对象
        class Res {
            private Integer contribution_id;
            private String sender_phone_number;
            private String sender_email;
            private Integer transaction_id;
            private BigDecimal contribution_amount;
            private Boolean is_contributed;

            public Integer getContribution_id() {
                return contribution_id;
            }

            public void setContribution_id(Integer contribution_id) {
                this.contribution_id = contribution_id;
            }

            public String getSender_phone_number() {
                return sender_phone_number;
            }

            public void setSender_phone_number(String sender_phone_number) {
                this.sender_phone_number = sender_phone_number;
            }

            public String getSender_email() {
                return sender_email;
            }

            public void setSender_email(String sender_email) {
                this.sender_email = sender_email;
            }

            public Integer getTransaction_id() {
                return transaction_id;
            }

            public void setTransaction_id(Integer transaction_id) {
                this.transaction_id = transaction_id;
            }

            public BigDecimal getContribution_amount() {
                return contribution_amount;
            }

            public void setContribution_amount(BigDecimal contribution_amount) {
                this.contribution_amount = contribution_amount;
            }

            public Boolean getIs_contributed() {
                return is_contributed;
            }

            public void setIs_contributed(Boolean is_contributed) {
                this.is_contributed = is_contributed;
            }
        }
        List<Res> ress = new ArrayList<>();
        for (RequestContribution rc : requestContributions) {
            Res res = new Res();
            res.setContribution_id(rc.getContributionId());
            res.setSender_phone_number(rc.getSenderPhoneNumber());
            res.setSender_email(rc.getSenderEmailId() == null ? null : userService.getEmailByEmailId(rc.getSenderEmailId()));
            res.setTransaction_id(rc.getTransactionId());
            res.setContribution_amount(rc.getContributionAmount());
            res.setIs_contributed(rc.getIsContributed());
            ress.add(res);
        }
        if (requestContributions.isEmpty()) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(ress) + ",\"message\":\"查询成功\"}");
    }

    @RequestMapping("/sendForGroup")
    public ResponseEntity<String> sendForGroup(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 向单条群收款付钱
        Integer send_id = jsonNode.get("user_id").asInt();
        Integer contributionId = jsonNode.get("contribution_id").asInt();
        String memo = jsonNode.get("memo") == null ? null : jsonNode.get("memo").asText();
        String password = jsonNode.get("password").asText();
        boolean isPayByWallet = jsonNode.get("isPayByWallet").asBoolean();
        TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
        TimeZone.setDefault(time);
        Date startTime = new Date();
        // 获取该条contribution支付方和收款方信息
        RequestContribution rc = accountOptService.searchGroupContributionByContributionIdService(contributionId);
        Request request = accountOptService.searchGroupRequestByRequestIdService(rc.getRequestId());
        memo = request.getMemo();

        // 查错（这个过程，不是应该由前端逻辑已经保证了传回来的是有效字段吗？？）
        if (rc.getTransactionId() != null) return ResponseEntity.ok("{\"status\":1,\"message\":\"该项群收款已支付过\"}");
        else if (password == null || password.isEmpty()) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(send_id);
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }

        // 插入transaction记录开始（成功返回的是该条transaction记录的transaction_id）
        // 使用电子钱包付款
        if (isPayByWallet) {
            BigDecimal walletMoney = nowUser.getBalance();
            if (walletMoney.compareTo(rc.getContributionAmount()) < 0) {
                return ResponseEntity.ok("{\"status\":1,\"message\":\"电子钱包余额不足\"}");
            } else {
                // 更新付款方电子钱包
                BigDecimal newWalletMoney = walletMoney.subtract(rc.getContributionAmount());
                int res = userService.updateBalanceByUserId(send_id, newWalletMoney);
                if (res == 0) return ResponseEntity.ok("{\"status\":1,\"message\":\"使用电子钱包支付失败\"}");
                // 更新收款方电子钱包
                BigDecimal otherMoney = userService.findUserPasswordService(request.getRequesterUserId()).getBalance();
                BigDecimal newOtherMoney = otherMoney.add(rc.getContributionAmount());
                int res1 = userService.updateBalanceByUserId(request.getRequesterUserId(), newOtherMoney);
                if (res1 == 0) {
                    // 付款方事务回滚
                    userService.updateBalanceByUserId(send_id, walletMoney);
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"使用电子钱包支付更新收款方余额失败\"}");
                }
                // 插入transaction记录
                int res2 = accountOptService.insertTransactionService(send_id, request.getRequesterUserId(), rc.getContributionAmount().doubleValue(), memo, null, null, startTime);
                if (res2 == 0) {
                    // 插入记录失败，回滚付款方和收款方事务
                    userService.updateBalanceByUserId(send_id, walletMoney);
                    userService.updateBalanceByUserId(request.getRequesterUserId(), otherMoney);
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"使用电子钱包支付插入交易记录失败\"}");
                }
                accountOptService.recordEndTimeTransactionService(res2);
                // 更新RequestContribution表记录（添加transaction_id）
                boolean isContribute = true;
                accountOptService.updateGroupContributionService(contributionId, res2, isContribute);
                return ResponseEntity.ok("{\"status\":0,\"data\":" + res2 + ",\"message\":\"使用电子钱包支付成功\"}");
            }
        }

        // 使用银行卡支付（前端还需提供支付方式，phone_number或email_address！！由于未提供，此处实现考虑用contribution表记录的方式）
        // 银行方面的操作默认成功，也即只要收款方wallet能收到，则记入新的transaction记录
        Integer emailId = rc.getSenderEmailId();
        String phoneNumber = rc.getSenderPhoneNumber();

        // 增加收款方余额（默认转入其钱包账户）
        BigDecimal otherMoney = userService.findUserPasswordService(request.getRequesterUserId()).getBalance();
        BigDecimal newOtherMoney = otherMoney.add(rc.getContributionAmount());
        int res = userService.updateBalanceByUserId(request.getRequesterUserId(), newOtherMoney);
        if (res == 0) return ResponseEntity.ok("{\"status\":1,\"message\":\"使用银行卡支付更新收款方余额失败\"}");

        int res1;
        // 使用银行卡：按照手机号付款
        if (emailId == null)
            res1 = accountOptService.insertTransactionService(send_id, request.getRequesterUserId(), rc.getContributionAmount().doubleValue(), memo, null, phoneNumber, startTime);
            // 使用银行卡：按照Email付款（若电话和邮件都填了，就默认用邮件支付）
        else
            res1 = accountOptService.insertTransactionService(send_id, request.getRequesterUserId(), rc.getContributionAmount().doubleValue(), memo, emailId, null, startTime);
        // 支付失败：收款方事务回滚
        if (res1 == 0) {
            userService.updateBalanceByUserId(request.getRequesterUserId(), otherMoney);
            return ResponseEntity.ok("{\"status\":1,\"message\":\"使用银行卡支付失败\"}");
        }

        accountOptService.recordEndTimeTransactionService(res1);
        // 更新RequestContribution表记录（添加transaction_id）
        boolean isContribute = true;
        accountOptService.updateGroupContributionService(contributionId, res1, isContribute);
        return ResponseEntity.ok("{\"status\":0,\"data\":" + res1 + ",\"message\":\"基于手机号或邮件支付成功\"}");
    }

    @RequestMapping("/searchGroupRequestBySenderId")
    public ResponseEntity<String> searchGroupRequestBySenderId(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查看所有我收到的群收款（未付款和已付款都有）
        Integer user_id = jsonNode.get("user_id").asInt();
        int[] emailIds = userService.getEmailIdByUserId(user_id);
        String phoneNumber = userService.getPhoneByUserId(user_id);

        List<RequestContribution> rcs = new ArrayList<>();
        List<RequestContribution> contributions1 = accountOptService.searchGroupContributionByPhoneService(phoneNumber);
        if (contributions1 != null && !contributions1.isEmpty()) rcs.addAll(contributions1);
        for (int emailId : emailIds) {
            List<RequestContribution> contributions2 = accountOptService.searchGroupContributionByEmailIdService(emailId);
            if (contributions2 != null && !contributions2.isEmpty()) rcs.addAll(contributions2);
        }

        // 返回新结构体
        class Res {
            private Integer request_id;
            private Integer contribution_id;
            private String name;    // 收款人
            private BigDecimal contribution_amount;
            private String memo;
            private Date request_time;
            private boolean is_contributed;

            public Integer getRequest_id() {
                return request_id;
            }

            public void setRequest_id(Integer request_id) {
                this.request_id = request_id;
            }

            public Integer getContribution_id() {
                return contribution_id;
            }

            public void setContribution_id(Integer contribution_id) {
                this.contribution_id = contribution_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public BigDecimal getContribution_amount() {
                return contribution_amount;
            }

            public void setContribution_amount(BigDecimal contribution_amount) {
                this.contribution_amount = contribution_amount;
            }

            public String getMemo() {
                return memo;
            }

            public void setMemo(String memo) {
                this.memo = memo;
            }

            public Date getRequest_time() {
                return request_time;
            }

            public void setRequest_time(Date request_time) {
                this.request_time = request_time;
            }

            public boolean getIs_contributed() {
                return is_contributed;
            }

            public void setIs_contributed(boolean is_contribute) {
                this.is_contributed = is_contribute;
            }
        }
        List<Res> ress = new ArrayList<>();
        for (RequestContribution rc : rcs) {
            Res res = new Res();
            res.setRequest_id(rc.getRequestId());
            res.setContribution_id(rc.getContributionId());
            res.setContribution_amount(rc.getContributionAmount());
            res.setMemo(accountOptService.searchGroupRequestByRequestIdService(rc.getRequestId()).getMemo());
            res.setRequest_time(accountOptService.searchGroupRequestByRequestIdService(rc.getRequestId()).getRequestTime());
            res.setName(userService.getNameByUserId(accountOptService.searchGroupRequestByRequestIdService(rc.getRequestId()).getRequesterUserId()));
            res.setIs_contributed(rc.getIsContributed());
            ress.add(res);
        }
        if (ress.isEmpty()) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(ress) + ",\"message\":\"查询成功\"}");
        }
    }

    private List<Transinfo> transtransaction(List<TransactionWithBLOBs> l) {
        List<Transinfo> res = new ArrayList<>();
        // 遍历每个transaction，转换成Transinfo
        for (int i = 0; i < l.size(); i++) {
            // 用l.get(i）初始化一个Transinfo
            Transinfo t = new Transinfo();
            t.setTransactionId(l.get(i).getTransactionId());
            t.setSenderUserId(l.get(i).getSenderUserId());
            t.setRecipientUserId(l.get(i).getRecipientUserId());
            t.setRecipientEmailId(l.get(i).getRecipientEmailId());
            t.setRecipientPhoneNumber(l.get(i).getRecipientPhoneNumber());
            t.setTransactionStartTime(l.get(i).getTransactionStartTime());
            t.setTransctionFinishedTime(l.get(i).getTransctionFinishedTime());
            t.setIsCancelled(l.get(i).getIsCancelled());
            t.setCancelledTime(l.get(i).getCancelledTime());
            t.setCancelledReason(l.get(i).getCancelledReason());
            t.setAmount(l.get(i).getAmount());
            t.setMemo(l.get(i).getMemo());
            // 获取Username
            t.setUserName(userService.getNameByUserId(l.get(i).getSenderUserId()));
            // 获取EmailAddress
            if (l.get(i).getRecipientEmailId() != null)
                t.setEmailAddress(userService.getEmailByEmailId2(l.get(i).getRecipientEmailId()));
            res.add(t);
        }
        return res;
    }
}
