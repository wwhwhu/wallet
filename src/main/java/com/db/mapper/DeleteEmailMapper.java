package com.db.mapper;

import com.db.entity.DeleteEmail;
import com.db.entity.Email;

public interface DeleteEmailMapper {
    DeleteEmail selectByPrimaryKey(Integer emailId);
    int insert(DeleteEmail record);

}
