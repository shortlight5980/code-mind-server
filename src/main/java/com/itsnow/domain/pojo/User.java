package com.itsnow.domain.pojo;

import java.time.LocalDateTime;
import java.util.Date;

import com.itsnow.enums.UserStatus;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@Data
public class User {
    /**
     * 主键，自增
     */
    private Long id;

    /**
     * 登录账号，唯一
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 加密密文
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否启用 (1:启用 0:禁用)
     */
    private UserStatus enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}