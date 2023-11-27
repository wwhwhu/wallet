package com.db.mapper;

import com.db.entity.Email;

public interface EmailMapper {
    int deleteByPrimaryKey(Integer emailId);

    int insert(Email record);

    int insertSelective(Email record);

    Email selectByPrimaryKey(Integer emailId);

    int updateByPrimaryKeySelective(Email record);

    int updateByPrimaryKey(Email record);

    Email selectByEmailAddress(String email);

    int updateEmailUser(Email record);

    int deleteByEmailAddr(Email record);
}