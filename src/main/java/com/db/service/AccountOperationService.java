package com.db.service;

import com.db.entity.Request;
import com.db.entity.RequestContribution;
import com.db.entity.Transaction;
import com.db.entity.TransactionWithBLOBs;
import com.db.util.BestSeller;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public interface AccountOperationService {
    // 存钱
    int insertTransactionService(Integer send_id, @Nullable Integer receive_id, Double amount, String meno, @Nullable Integer email_id, @Nullable String phone_number, Date startTime);
    // 记录结束时间
    int recordEndTimeTransactionService(Integer transaction_id);
    // 更新User_id
    int updateUserIndo(Integer transactionId, int user_id);
    // 根据年月查找Transaction记录
    List<TransactionWithBLOBs> searchTransactionPerMonthService(int user_id, int year, int mon);
    // 根据UserId查找Transaction记录
    List<TransactionWithBLOBs> searchTransactionByUserIdService(int user_id, int receive_id);
    // 根据Email查找Transaction记录
    List<TransactionWithBLOBs> searchTransactionByEmailService(int user_id,String email);
    // 根据Phone查找Transaction记录
    List<TransactionWithBLOBs> searchTransactionByPhoneService(int user_id,String phone);
    // 根据时间范围查找Transaction记录
    List<TransactionWithBLOBs> searchTransactionByDateRangeService(int user_id,Date start,Date end);
    // 查找取消的Transaction记录
    List<TransactionWithBLOBs> searchTransactionCancelledService(int user_id);
    // 取消Transaction记录
    int cancelTransactionService(int user_id, int transaction_id, String meno);
    // 根据TransactionId查找Transaction记录
    TransactionWithBLOBs getTransactionByIdService(int transaction_id);
    // 查询最高的转账对象
    List<BestSeller> searchBestSellerService(int user_id,Date start,Date end);
    // 插入群收款请求request记录
    int insertRequestService(Integer requesterUserId, BigDecimal totalAmount,Date requestTime, String memo);
    // 插入群收款每个收款对象RequestContribution记录
    int[] insertRequestContributionService(List<RequestContribution> contributions);
    // 根据userId查找所有发起的群收款记录
    List<Request> searchGroupRequestForRequesterByUserIdService(Integer user_id);
    // 根据requestId查找发起的群收款记录
    Request searchGroupRequestForRequesterById(Integer requestId);
    // 根据requestId查找发起的群收款的每一个contribution记录
    List<RequestContribution> searchGroupContributionForRequesterById(Integer requestId);
}
