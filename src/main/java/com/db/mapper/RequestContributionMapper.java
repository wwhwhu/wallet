package com.db.mapper;

import com.db.entity.RequestContribution;

public interface RequestContributionMapper {
    int deleteByPrimaryKey(Integer contributionId);

    int insert(RequestContribution record);

    int insertSelective(RequestContribution record);

    RequestContribution selectByPrimaryKey(Integer contributionId);

    int updateByPrimaryKeySelective(RequestContribution record);

    int updateByPrimaryKey(RequestContribution record);
}