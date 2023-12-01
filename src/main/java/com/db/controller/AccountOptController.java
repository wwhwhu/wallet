package com.db.controller;

import com.db.entity.Email;
import com.db.entity.TransactionWithBLOBs;
import com.db.entity.User;
import com.db.service.AccountOperationService;
import com.db.service.UserInfoService;
import com.db.util.BestSeller;
import com.db.util.MonthStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        String email_id = jsonNode.get("email_address").asText();
        String phone_number = jsonNode.get("phone_number").asText();
        String amount = jsonNode.get("amount").asText();
        String meno = jsonNode.get("memo").asText();
        TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
        TimeZone.setDefault(time);
        Date startTime = new Date();
        // 使用电子钱包还是银行卡
        String fun = jsonNode.get("isPayByWallet").asText();
        if (send_id == null || send_id.equals("") || password == null || password.equals("") || amount == null || amount.equals("")  || fun == null || fun.equals("")) {
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
        if(isPayByWallet)
        {
            BigDecimal walletmoney = nowUser.getBalance();
            if(walletmoney.compareTo(BigDecimal.valueOf(Double.parseDouble(amount)))<0)
            {
                return ResponseEntity.ok("{\"status\":1,\"message\":\"电子钱包余额不足\"}");
            }
        }
        // 按照手机号转账
        if (email_id == null || email_id.equals("")) {
            int res0 = userService.getTransactionUserInfoByPhone(phone_number);
            if(res0 == 0 || res0 == -3) {
                // 向无账户者转账
                // 首先增加一个phone记录
                if(res0==0) {
                    int res = userService.insertPhoneService(phone_number, null, false, false);
                    if (res == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"插入手机号错误\"}");
                    }
                }
                // 然后增加一个Transaction记录
                int res2 = accountOptService.insertTransactionService(Integer.parseInt(send_id),null,Double.parseDouble(amount),meno,null,phone_number,startTime);
                if(res2 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"向新账户转账失败\"}");
                }
                if(isPayByWallet)
                {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id),newwalletmoney);
                    if(res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 成功向无账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":"+res2+",\"message\":\"向新账户转账成功\"}");
            }else
            {
                if(Integer.parseInt(send_id)==res0)
                {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"不能向自己转账\"}");
                }
                // 向有账户者转账
                int res = accountOptService.insertTransactionService(Integer.parseInt(send_id),res0,Double.parseDouble(amount),meno,null,phone_number,startTime);
                if(res == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"已有账户转账失败\"}");
                }
                if(isPayByWallet)
                {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id),newwalletmoney);
                    if(res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 增加他人余额
                BigDecimal othermoney = userService.findUserPasswordService(res0).getBalance();
                BigDecimal newothermoney = othermoney.add(BigDecimal.valueOf(Double.parseDouble(amount)));
                int res4 = userService.updateBalanceByUserId(res0,newothermoney);
                if(res4 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"更新他人余额失败\"}");
                }
                accountOptService.recordEndTimeTransactionService(res);
                // 成功向有账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":"+res+",\"message\":\"已有账户转账成功\"}");
            }
        }
        // 按照Email转账
        if (phone_number == null || phone_number.equals("")) {
            Email res0 = userService.getTransactionUserInfoByEmail(email_id);
            if(res0 == null || !res0.getIsRegistered()) {
                // 向无账户者转账
                // 首先增加一个Email记录
                int email_id0;
                if(res0 == null) {
                    int res = userService.insertEmailService(null, email_id, false, false);
                    if (res == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"插入邮箱错误\"}");
                    }
                    email_id0 = userService.getTransactionUserInfoByEmail(email_id).getEmailId();
                }else{
                    email_id0 = res0.getEmailId();  // 有账户但未注册
                }
                // 然后增加一个Transaction记录
                int res2 = accountOptService.insertTransactionService(Integer.parseInt(send_id),null,Double.parseDouble(amount),meno,email_id0,null,startTime);
                if(res2 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"向新账户转账失败\"}");
                }
                if(isPayByWallet)
                {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id),newwalletmoney);
                    if(res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 成功向无账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":"+res2+",\"message\":\"向新账户转账成功\"}");
            }else
            {
                if(Integer.parseInt(send_id)==res0.getUserId())
                {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"不能向自己转账\"}");
                }
                // 向有账户者转账
                int res = accountOptService.insertTransactionService(Integer.parseInt(send_id),res0.getUserId(),Double.parseDouble(amount),meno,res0.getEmailId(),null,startTime);
                if(res == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"已有账户转账失败\"}");
                }
                if(isPayByWallet)
                {
                    // 更新电子钱包
                    BigDecimal walletmoney = nowUser.getBalance();
                    BigDecimal newwalletmoney = walletmoney.subtract(BigDecimal.valueOf(Double.parseDouble(amount)));
                    int res3 = userService.updateBalanceByUserId(Integer.parseInt(send_id),newwalletmoney);
                    if(res3 == 0) {
                        return ResponseEntity.ok("{\"status\":1,\"message\":\"更新电子钱包失败\"}");
                    }
                }
                // 增加他人余额
                BigDecimal othermoney = userService.findUserPasswordService(res0.getUserId()).getBalance();
                BigDecimal newothermoney = othermoney.add(BigDecimal.valueOf(Double.parseDouble(amount)));
                int res4 = userService.updateBalanceByUserId(res0.getUserId(),newothermoney);
                if(res4 == 0) {
                    return ResponseEntity.ok("{\"status\":1,\"message\":\"更新他人余额失败\"}");
                }
                accountOptService.recordEndTimeTransactionService(res);
                // 成功向有账户者转账
                return ResponseEntity.ok("{\"status\":0,\"data\":"+res+",\"message\":\"已有账户，转账成功\"}");
            }
        }
        else{
            return ResponseEntity.ok("{\"status\":1,\"message\":\"无法同时输入Email和电话号码\"}");
        }
    }

    @RequestMapping("/searchTransactionPerMonth")
    public ResponseEntity<String> searchTransactionPerMonth(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 统计以开始时间为准的一个月内的交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String year = jsonNode.get("year").asText();
        String month = jsonNode.get("month").asText();
        if(year == null || year.equals("") || month == null || month.equals("") || user_id == null || user_id.equals("") || password == null || password.equals(""))
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionPerMonthService(Integer.parseInt(user_id),Integer.parseInt(year),Integer.parseInt(month));
        if(res == null)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        }
        else if(res.size() == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        }else{
            // 统计总金额与平均金额,统计最大金额与其对应的交易ID
            BigDecimal total = new BigDecimal(0);
            BigDecimal max = new BigDecimal(0);
            // 统计的时候不包括取消的交易
            int maxId = 0;
            int j = 0;
            for(int i=0;i<res.size();i++)
            {
                if(!res.get(i).getIsCancelled()) {
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
            for(int i=0;i<res.size();i++)
            {
                if(res.get(i).getAmount().compareTo(max) == 0 && !res.get(i).getIsCancelled())
                {
                    maxIdList.add(res.get(i).getTransactionId());
                }
            }
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
            monthStatistics.setMonthStatisticsList(res);
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(monthStatistics)+",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionBySSN")
    public ResponseEntity<String> searchTransactionBySSN(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException{
        // 通过SSN查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String ssn = jsonNode.get("ssn").asText();
        if(ssn == null || ssn.equals("") || user_id == null || user_id.equals("") || password == null || password.equals(""))
        {
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
        if(userId == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"无此SSN用户\"}");
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByUserIdService(Integer.parseInt(user_id), userId);
        if(res == null)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        }
        else if(res.size() == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        }else{
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(res)+",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionByEmail")
    public ResponseEntity<String> searchTransactionByEmail(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过Email查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String email = jsonNode.get("email_address").asText();
        if(email == null || email.equals("") || user_id == null || user_id.equals("") || password == null || password.equals(""))
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByEmailService(Integer.parseInt(user_id), email);
        if(res == null)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        }
        else if(res.size() == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        }else{
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(res)+",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchTransactionByPhone")
    public ResponseEntity<String> searchTransactionByPhone(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 通过Phone查询交易记录
        String user_id = jsonNode.get("user_id").asText();
        String password = jsonNode.get("password").asText();
        String phone = jsonNode.get("phone").asText();
        if(phone == null || phone.equals("") || user_id == null || user_id.equals("") || password == null || password.equals(""))
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionByPhoneService(Integer.parseInt(user_id), phone);
        if(res == null)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        }
        else if(res.size() == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        }else{
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(res)+",\"message\":\"查询成功\"}");
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
        if(endDate.before(startDate))
        {
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
                if(!transaction.getIsCancelled())
                {
                    total = total.add(transaction.getAmount());
                }
            }
            // 封装返回数据
            class Res{
                private List<TransactionWithBLOBs> data;
                private BigDecimal total;
                public List<TransactionWithBLOBs> getData() {
                    return data;
                }
                public void setData(List<TransactionWithBLOBs> data) {
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
            res1.setData(res);
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
        if(user_id == null || user_id.equals("") || password == null || password.equals(""))
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"非法空字段\"}");
        }
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<TransactionWithBLOBs> res = accountOptService.searchTransactionCancelledService(Integer.parseInt(user_id));
        if(res == null)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        }
        else if(res.size() == 0)
        {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        }else{
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(res)+",\"message\":\"查询成功\"}");
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
        } else if (res == -3){
            return ResponseEntity.ok("{\"status\":1,\"message\":\"超过10分钟无法取消\"}");
        }else if(res==0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"取消失败\"}");
        }else{
            // 获取当前已取消Transaction返回
            TransactionWithBLOBs transactionWithBLOBs = accountOptService.getTransactionByIdService(Integer.parseInt(transaction_id));
            return ResponseEntity.ok("{\"status\":0,\"data\":"+new ObjectMapper().writeValueAsString(transactionWithBLOBs)+",\"message\":\"查询成功\"}");
        }
    }

    @RequestMapping("/searchBestSeller")
    public ResponseEntity<String> searchBestSeller(@RequestBody JsonNode jsonNode, HttpSession session) throws JsonProcessingException {
        // 查询最佳卖家
        String user_id = jsonNode.get("user_id").asText();
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
        User nowUser = userService.findUserPasswordService(Integer.parseInt(user_id));
        String realPassword = nowUser.getPassword();
        if (!password.equals(realPassword)) {
            String jsonResponse = "{\"status\":1,\"message\":\"密码输入错误\"}";
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
        List<BestSeller> res = accountOptService.searchBestSellerService(Integer.parseInt(user_id), startDate, endDate);
        if (res == null) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询失败\"}");
        } else if (res.size() == 0) {
            return ResponseEntity.ok("{\"status\":1,\"message\":\"查询结果为空\"}");
        } else {
            return ResponseEntity.ok("{\"status\":0,\"data\":" + new ObjectMapper().writeValueAsString(res.get(0)) + ",\"message\":\"查询成功\"}");
        }
    }
}
