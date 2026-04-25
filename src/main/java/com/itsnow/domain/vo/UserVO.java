package com.itsnow.domain.vo;

import lombok.Data;


/**
 * 用户表
 * @TableName user
 */
@Data
public class UserVO {
    /**
     * 主键，自增
     */
    private Long id;

    /**
     * 登录账号，唯一
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}