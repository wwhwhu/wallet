package com.db.service.impl;

import com.db.entity.Request;
import com.db.entity.RequestContribution;
import com.db.entity.TransactionWithBLOBs;
import com.db.entity.User;
import com.db.mapper.RequestContributionMapper;
import com.db.mapper.RequestMapper;
import com.db.mapper.TransactionMapper;
import com.db.mapper.UserMapper;
import com.db.service.AccountOperationService;
import com.db.util.BestSeller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

@Service
public class AccountOperationServiceImpl implements AccountOperationService {

    private final TransactionMapper transactionMapper;
    private final UserMapper userMapper;
    private final RequestMapper requestMapper;
    private final RequestContributionMapper requestContributionMapper;

    @Autowired
    public AccountOperationServiceImpl(TransactionMapper transactionMapper, UserMapper userMapper, RequestMapper requestMapper, RequestContributionMapper requestContributionMapper)
    {
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
        this.requestMapper = requestMapper;
        this.requestContributionMapper = requestContributionMapper;
    }

    @Override
    public int insertTransactionService(Integer send_id, @Nullable Integer receive_id, Double amount, String meno, @Nullable Integer email_id, @Nullable String phone_number, Date startTime){
        TransactionWithBLOBs transaction = new TransactionWithBLOBs();
        transaction.setSenderUserId(send_id);
        transaction.setRecipientUserId(receive_id);
        transaction.setRecipientEmailId(email_id);
        transaction.setRecipientPhoneNumber(phone_number);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setMemo(meno);
        transaction.setTransactionStartTime(startTime);
        transaction.setIsCancelled(false);
        int res = transactionMapper.insert(transaction);
        if(res==0)
            return 0;
        return transaction.getTransactionId();
    }

    @Override
    public int recordEndTimeTransactionService(Integer transaction_id) {
        TransactionWithBLOBs transaction = new TransactionWithBLOBs();
        transaction.setTransactionId(transaction_id);
        TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
        TimeZone.setDefault(time);
        transaction.setTransctionFinishedTime(new Date());
        return transactionMapper.updateByPrimaryKeySelective(transaction);
    }

    @Override
    public int updateUserIndo(Integer transactionId, int user_id) {
        TransactionWithBLOBs transaction = new TransactionWithBLOBs();
        transaction.setTransactionId(transactionId);
        transaction.setRecipientUserId(user_id);
        return transactionMapper.updateByPrimaryKeySelective(transaction);
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionPerMonthService(int user_id, int year, int mon) {
        // 参数添加到HashMap
        HashMap<String, Integer> map = new HashMap<>();
        map.put("userId", user_id);
        map.put("year", year);
        map.put("month", mon);
        List<TransactionWithBLOBs> res = transactionMapper.searchTransactionPerMonth(map);
        return res;
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionByUserIdService(int user_id, int receive_id) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("userId", user_id);
        map.put("receiveId", receive_id);
        return transactionMapper.selectByUserId(map);
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionByEmailService(int user_id, String email) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", user_id);
        map.put("email", email);
        return transactionMapper.selectByEmail(map);
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionByPhoneService(int user_id, String phone) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", user_id);
        map.put("recipientPhoneNumber", phone);
        return transactionMapper.selectByPhoneNum(map);
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionByDateRangeService(int user_id, Date start, Date end) {
        HashMap<String, Object> map = new HashMap<>();
        // 给end日期加一天
        end.setTime(end.getTime() + 24*60*60*1000);
        map.put("userId", user_id);
        map.put("startTime", start);
        map.put("endTime", end);
        return transactionMapper.selectByDateRange(map);
    }

    @Override
    public List<TransactionWithBLOBs> searchTransactionCancelledService(int user_id) {
        return transactionMapper.selectCancelledTransaction(user_id);
    }

    @Override
    public int cancelTransactionService(int user_id, int transaction_id, String meno) {
        TransactionWithBLOBs transaction = transactionMapper.selectByPrimaryKey(transaction_id);
        // 如果不是自己的交易，返回-1
        if(transaction==null || transaction.getSenderUserId() != user_id)
            return -1;
        // 如果已经取消或者已经完成，返回-2
        else if(transaction.getIsCancelled() || transaction.getCancelledTime() != null)
            return -2;
        else {
            // 如果当前时间剪去开始时间小于10分钟，可以取消，返回-2
            Date startTime = transaction.getTransactionStartTime();
            Date now = new Date();
            long diff = now.getTime() - startTime.getTime();
            long diffMinutes = diff / (60 * 1000) % 60;
            if(diffMinutes < 10){
                TransactionWithBLOBs t = new TransactionWithBLOBs();
                t.setTransactionId(transaction_id);
                t.setIsCancelled(true);
                t.setCancelledTime(now);
                t.setCancelledReason(meno);
                // 返回金额
                BigDecimal amount = transaction.getAmount();
                int res1 = transactionMapper.updateByPrimaryKeySelective(t);
                if(transaction.getRecipientUserId() != null){
                    // 更新收款人的余额
                    User u = userMapper.selectByPrimaryKey(transaction.getRecipientUserId());
                    BigDecimal balance = u.getBalance();
                    balance = balance.subtract(amount);
                    User uu = new User();
                    uu.setUserId(transaction.getRecipientUserId());
                    uu.setBalance(balance);
                    int res2 =  userMapper.updateByPrimaryKeySelective(uu);
                    // 更新发送者的余额
                    User u2 = userMapper.selectByPrimaryKey(user_id);
                    BigDecimal balance2 = u2.getBalance();
                    balance2 = balance2.add(amount);
                    User uu2 = new User();
                    uu2.setUserId(user_id);
                    uu2.setBalance(balance2);
                    int res3 =  userMapper.updateByPrimaryKeySelective(uu2);
                    if(res1 == 1 && res2 == 1 && res3 == 1)
                        return 1;
                    return 0;
                }
                else{
                    // 更新发送者的余额
                    User u2 = userMapper.selectByPrimaryKey(user_id);
                    BigDecimal balance2 = u2.getBalance();
                    balance2 = balance2.add(amount);
                    User uu2 = new User();
                    uu2.setUserId(user_id);
                    uu2.setBalance(balance2);
                    int res3 =  userMapper.updateByPrimaryKeySelective(uu2);
                    if(res1 == 1 && res3 == 1)
                        return 1;
                    return 0;
                }
            }
            else{
                // 超过10分钟
                return -3;
            }
        }
    }

    @Override
    public TransactionWithBLOBs getTransactionByIdService(int transaction_id)
    {
        return transactionMapper.selectByPrimaryKey(transaction_id);
    }

    @Override
    public List<BestSeller> searchBestSellerService(Integer user_id, Date start, Date end) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", user_id);
        map.put("startTime", start);
        // 给end日期加一天
        end.setTime(end.getTime() + 24*60*60*1000);
        map.put("endTime", end);
        return transactionMapper.selectBestSeller(map);
    }

    @Override
    public int insertRequestService(Integer requesterUserId, BigDecimal totalAmount,Date requestTime, String memo){
        Request request = new Request();
        request.setRequesterUserId(requesterUserId);
        request.setTotalAmount(totalAmount);
        request.setRequestTime(requestTime);
        request.setMemo(memo);
        int res = requestMapper.insert(request);
        if(res==0)
            return 0;
        return request.getRequestId();
    }

    @Override
    public int[] insertRequestContributionService(List<RequestContribution> contributions){
        // Integer requestId, String senderPhoneNumber, String sender_email_id, BigDecimal contribution_amount
        int[] res = new int[contributions.size()];
        for(int i=0;i<contributions.size();i++){
            requestContributionMapper.insert(contributions.get(i));
            res[i] = contributions.get(i).getContributionId();
        }
        return res;
    }

    @Override
    public List<Request> searchGroupRequestByUserIdService(Integer user_id){
        return requestMapper.selectByUserId(user_id);
    }

    @Override
    public Request searchGroupRequestByRequestIdService(Integer requestId){
        return requestMapper.selectByPrimaryKey(requestId);
    }

    @Override
    public List<RequestContribution> searchGroupContributionByRequestIdService(Integer requestId){
        return requestContributionMapper.selectByRequestId(requestId);
    }

    @Override
    public List<RequestContribution> searchGroupContributionByEmailIdService(Integer emailId){
        return requestContributionMapper.selectByEmailId(emailId);
    }

    @Override
    public List<RequestContribution> searchGroupContributionByPhoneService(String phoneNumber){
        return requestContributionMapper.selectByPhone(phoneNumber);
    }

    @Override
    public RequestContribution searchGroupContributionByContributionIdService(Integer contributionId){
        return requestContributionMapper.selectByPrimaryKey(contributionId);
    }

    @Override
    public int updateGroupContributionService(Integer contributionId, Integer transactionId, boolean isPaid){
        RequestContribution rc = new RequestContribution();
        rc.setContributionId(contributionId);
        rc.setTransactionId(transactionId);
        rc.setContributed(isPaid);
        return requestContributionMapper.updateByPrimaryKeySelective(rc);
    }
}
