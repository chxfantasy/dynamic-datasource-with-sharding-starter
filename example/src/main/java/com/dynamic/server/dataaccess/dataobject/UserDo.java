package com.dynamic.server.dataaccess.dataobject;

import lombok.Data;

import java.util.Date;

/**
 * Created by Harry on 2018/10/7.
 */
@Data
public class UserDo {

    private String userId;
    private String nickName;
    private Date gmtCreate;

}
