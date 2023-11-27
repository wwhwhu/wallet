package com.db.mapper;

import com.db.entity.User;
import com.db.util.PersonalAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByEmail(@Param("email") String email);

    User selectByPhoneNumber(@Param("phone_number") String phone_number);

    Integer findBySSN(@Param("ssn") String ssn);

    List<PersonalAll> searchAllPersonalInfo(Integer user_id);
}