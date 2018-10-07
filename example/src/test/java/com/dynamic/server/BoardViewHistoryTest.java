package com.dynamic.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.bixin.DataSourceType;
import com.dynamic.server.dataaccess.dataobject.BoardViewHistoryDo;
import com.dynamic.server.dataaccess.dataobject.UserDo;
import com.dynamic.server.dataaccess.mapper.BoardViewHistoryDoMapper;
import com.dynamic.server.dataaccess.mapper.UserDoMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Created by Harry on 2018/10/7.
 */
public class BoardViewHistoryTest extends BaseServiceTest {

    @Autowired private BoardViewHistoryDoMapper boardViewHistoryDoMapper;

    @Test
    @Order(1)
    public void testInsert() {
        //set datasource inline
//        DynamicDataSourceContextHolder.setDataSourceLookupKey(DataSourceType.SHARDING_DATASOURCE);
        this.boardViewHistoryDoMapper.insert("1111", "2222");
        this.boardViewHistoryDoMapper.insert("1111", "3333");
        this.boardViewHistoryDoMapper.insert("2222", "1111");
        this.boardViewHistoryDoMapper.insert("3333333", "3333");
    }

    @Test
    @Order(2)
    public void testSelect() {
        //set datasource inline
//        DynamicDataSourceContextHolder.setDataSourceLookupKey(DataSourceType.SHARDING_DATASOURCE);
        List<BoardViewHistoryDo> viewList = this.boardViewHistoryDoMapper.listViewHistorybyUserId("1111");
        System.out.println(JSON.toJSONString(viewList) );

        viewList = this.boardViewHistoryDoMapper.listViewHistorybyUserId("3333333");
        System.out.println(JSON.toJSONString(viewList) );

    }

}
