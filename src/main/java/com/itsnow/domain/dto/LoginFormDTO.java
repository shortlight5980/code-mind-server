package com.itsnow.domain.dto;

import com.itsnow.enums.LoginType;
import lombok.Data;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@Data
public class LoginFormDTO {

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 短信验证码
     */
    private String code;

    /**
     * 密码（login_type = password 时必填，RSA 加密后传输）
     */
    private String password;

    /**
     * 账号或邮箱
     */
    private String account;

    /**
     * 图形验证码 ID（可选，用于防暴力破解）
     */
    private String captchaId;

    /**
     * 图形验证码值（可选，与 captchaId 配合使用）
     */
    private String captchaValue;


}
