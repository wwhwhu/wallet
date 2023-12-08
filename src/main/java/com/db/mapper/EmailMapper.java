package com.db.mapper;

import com.db.entity.Email;

import java.util.List;

public interface EmailMapper {
    int deleteByPrimaryKey(Integer emailId);

    int insert(Email record);

    int insertSelective(Email record);

    Email selectByPrimaryKey(Integer emailId);

    int updateByPrimaryKeySelective(Email record);

    int updateByPrimaryKey(Email record);

    Email selectByEmailAddress(String email);

    List<Email> selectByUserId(Integer userId);

    int updateEmailUser(Email record);

    int deleteByEmailAddr(Email record);
}