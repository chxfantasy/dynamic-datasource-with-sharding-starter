package com.dynamic.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.dynamic.server.dataaccess.dataobject.UserDo;
import com.dynamic.server.dataaccess.mapper.UserDoMapper;
import com.dynamic.server.datasourceconfig.DataSourceType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

/**
 * Created by Harry on 2018/10/7.
 */
public class UserServiceTest extends BaseServiceTest {

    @Autowired private UserDoMapper userDoMapper;

    @Test
    @Order(1)
    public void testInsertUser() {
        //set datasource inline
//        DynamicDataSourceContextHolder.setDataSourceLookupKey(DataSourceType.DEFAULT);
        this.userDoMapper.insert("1111", "1");
        this.userDoMapper.insert("2222", "2");
        this.userDoMapper.insert("3333", "3");
        this.userDoMapper.insert("4444", "4");
    }

    @Test
    @Order(2)
    public void testSelectUser() {
        //set datasource inline
//        DynamicDataSourceContextHolder.setDataSourceLookupKey(DataSourceType.DEFAULT);
        UserDo userDo = this.userDoMapper.selectByUserId( "1111" );
        System.out.println(JSON.toJSONString(userDo) );

        //set datasource inline
//        DynamicDataSourceContextHolder.setDataSourceLookupKey(DataSourceType.SLAVE);
        userDo = this.userDoMapper.selectByUserId( "1111" );
        System.out.println(JSON.toJSONString(userDo) );

    }

}
