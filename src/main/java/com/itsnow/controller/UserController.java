package com.itsnow.controller;

import com.itsnow.domain.dto.LoginFormDTO;
import com.itsnow.domain.dto.RegistFormDTO;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.vo.UserVO;
import com.itsnow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 登录
     * @param loginForm
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm) {
        log.info("用户登录：{}", loginForm);
        return userService.login(loginForm);
    }

    /**
     *  注册
     * @param registFormDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/regist")
    public Result regist(@RequestBody RegistFormDTO registFormDTO) throws Exception {
        log.info("用户注册：{}", registFormDTO);
        return userService.regist(registFormDTO);
    }

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @GetMapping("/info")
    public Result<UserVO> info(HttpServletRequest request) {
        return userService.info(request);
    }
}
