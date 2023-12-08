package com.db.mapper;

import com.db.entity.RequestContribution;

import java.util.List;

public interface RequestContributionMapper {
    int deleteByPrimaryKey(Integer contributionId);

    int insert(RequestContribution record);

    int insertSelective(RequestContribution record);

    RequestContribution selectByPrimaryKey(Integer contributionId);

    List<RequestContribution> selectByRequestId(Integer requestId);

    List<RequestContribution> selectByEmailId(Integer senderEmailId);

    List<RequestContribution> selectByPhone(String senderPhoneNumber);

    int updateByPrimaryKeySelective(RequestContribution record);

    int updateByPrimaryKey(RequestContribution record);
}