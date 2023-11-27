package com.db.mapper;

import com.db.entity.Transaction;
import com.db.entity.TransactionWithBLOBs;
import com.db.util.BestSeller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface TransactionMapper {
    int deleteByPrimaryKey(Integer transactionId);

    int insert(TransactionWithBLOBs record);

    int insertSelective(TransactionWithBLOBs record);

    TransactionWithBLOBs selectByPrimaryKey(Integer transactionId);

    int updateByPrimaryKeySelective(TransactionWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TransactionWithBLOBs record);

    int updateByPrimaryKey(Transaction record);

    List<TransactionWithBLOBs> selectByPhone(String phone);

    List<TransactionWithBLOBs> selectByEmail(HashMap<String, Object> map);

    List<TransactionWithBLOBs> selectByPhoneNum(HashMap<String, Object> map);

    List<TransactionWithBLOBs> selectByEmailId(Integer emailId);

    List<TransactionWithBLOBs> searchTransactionPerMonth(HashMap<String, Integer> map);

    List<TransactionWithBLOBs> selectByUserId(HashMap<String, Integer> map);

    List<TransactionWithBLOBs> selectByDateRange(HashMap<String, Object> map);

    List<TransactionWithBLOBs> selectCancelledTransaction(int user_id);

    List<BestSeller> selectBestSeller(HashMap<String, Object> map);
}