package com.dynamic.server.dataaccess.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.bixin.DataSourceType;
import com.dynamic.server.dataaccess.dataobject.BoardViewHistoryDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by Harry on 2018/10/7.
 */
@DS(DataSourceType.SHARDING_DATASOURCE)
public interface BoardViewHistoryDoMapper {

    BoardViewHistoryDo selectByPrimaryKey(Long id);

    @Select("select * from board_view_history where user_id = #{userId}")
    List<BoardViewHistoryDo> listViewHistorybyUserId(@Param("userId") String userId);

    @Insert("insert into board_view_history(`user_id`, `target_user_id`, `gmt_created`) value(#{userId}, #{targetUserId}, NOW())")
    int insert(@Param("userId") String userId, @Param("targetUserId") String targetUserId);

    int insertSelective(BoardViewHistoryDo boardViewHistoryDo);

}
