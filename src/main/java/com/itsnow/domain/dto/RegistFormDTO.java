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

    /**
     * 邮箱
     */
    private String email;

    /**
     * 图形验证码 ID（可选，用于防暴力破解）
     */
    private String captchaId;

    /**
     * 图形验证码值（可选，与 captchaId 配合使用）
     */
    private String captchaValue;
}
