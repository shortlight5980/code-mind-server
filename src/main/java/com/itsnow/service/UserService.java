package com.itsnow.service;

import com.itsnow.domain.dto.LoginFormDTO;
import com.itsnow.domain.dto.RegistFormDTO;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itsnow.domain.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 14144
* @createDate 2026-04-22 12:45:40
*/
public interface UserService extends IService<User> {

    /**
     * 登录
     * @param loginForm
     * @return
     */
    Result<String> login(LoginFormDTO loginForm) throws Exception;

    /**
     *  注册
     * @param registFormDTO
     * @return
     * @throws Exception
     */
    Result regist(RegistFormDTO registFormDTO) throws Exception;

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    Result<UserVO> info(HttpServletRequest request);

    /**
     *  发送验证码
     * @param phone
     * @return
     */
    Result sendCode(String phone);
}
