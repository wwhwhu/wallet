package com.db.mapper;

import com.db.entity.Request;

public interface RequestMapper {
    int deleteByPrimaryKey(Integer requestId);

    int insert(Request record);

    int insertSelective(Request record);

    Request selectByPrimaryKey(Integer requestId);

    int updateByPrimaryKeySelective(Request record);

    int updateByPrimaryKeyWithBLOBs(Request record);

    int updateByPrimaryKey(Request record);
}