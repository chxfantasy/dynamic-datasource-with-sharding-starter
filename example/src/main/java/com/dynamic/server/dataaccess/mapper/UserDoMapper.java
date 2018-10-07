package com.dynamic.server.dataaccess.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.dynamic.server.dataaccess.dataobject.UserDo;
import com.dynamic.server.datasourceconfig.DataSourceType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by Harry on 2018/10/7.
 */

public interface UserDoMapper {

    @DS(DataSourceType.SLAVE)
    @Select("select * from user where user_id = #{userId}")
    UserDo selectByUserId(@Param("userId") String userId);

    @DS(DataSourceType.DEFAULT)
    @Insert("insert into user(`user_id`, `nick_name`, `gmt_create`) value(#{userId}, #{nickName}, NOW())")
    int insert( @Param("userId") String userId, @Param("nickName") String nickName);

}
