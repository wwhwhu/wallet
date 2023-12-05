package com.db.mapper;

import com.db.entity.Request;
import com.db.entity.TransactionWithBLOBs;

import java.util.HashMap;
import java.util.List;

public interface RequestMapper {
    int deleteByPrimaryKey(Integer requestId);

    int insert(Request record);

    int insertSelective(Request record);

    Request selectByPrimaryKey(Integer requestId);

    List<Request> selectByUserId(Integer userId);

    int updateByPrimaryKeySelective(Request record);

    int updateByPrimaryKeyWithBLOBs(Request record);

    int updateByPrimaryKey(Request record);
}