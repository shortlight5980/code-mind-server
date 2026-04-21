package com.itsnow.controller;

import com.itsnow.domain.pojo.Result;
import com.itsnow.service.SessionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author itsnow
 * @date 2026/4/21
 */
@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    private SessionsService sessionsService;

    /**
     * 获取会话历史
     * @param id
     * @return
     */
    @GetMapping
    public Result getSession(Long id) {
        return sessionsService.getByUserId(id);
    }

    /**
     * 新增会话
     */
    @PostMapping()
    public Result addSession() {
        return sessionsService.addSession();
    }
}
