package com.itsnow.domain.dto;

import com.itsnow.enums.LoginType;
import lombok.Data;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@Data
public class RegistFormDTO {
    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信验证码
     */
    private String code;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名
     */
    private String username;

}
