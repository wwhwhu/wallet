package com.db.mapper;

import com.db.entity.PhoneNumber;

public interface PhoneNumberMapper {
    int deleteByPrimaryKey(String phoneNumber);

    int insert(PhoneNumber record);

    int insertSelective(PhoneNumber record);

    PhoneNumber selectByPrimaryKey(String phoneNumber);

    int updateByPrimaryKeySelective(PhoneNumber record);

    int updateByPrimaryKey(PhoneNumber record);

    int updatePhoneUser(PhoneNumber record);

    int deleteByPhoneNumber(PhoneNumber record);

    PhoneNumber selectPhoneByUser(Integer userId);
}