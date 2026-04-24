package com.itsnow.controller;

import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.vo.SessionsVO;
import com.itsnow.service.SessionsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.List;

/**
 * @author itsnow
 * @date 2026/4/21
 */
@RestController
@RequestMapping("/session")
@Slf4j
public class SessionController {
    @Autowired
    private SessionsService sessionsService;

    /**
     * 获取会话历史
     *
     * @param id
     * @return
     */
    @GetMapping
    public Result<List<SessionsVO>> getSession(Long id) {
        log.info("获取会话历史");
        return sessionsService.getByUserId(id);
    }

    /**
     * 新增会话
     */
    @PostMapping()
    public Result addSession(HttpServletRequest request) {
        log.info("新增会话");
        return sessionsService.addSession(request);
    }

    /**
     * 归档会话
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public Result archiveSession(@PathVariable Long id) {
        log.info("归档会话，id={}", id);
        return sessionsService.archiveSession(id);
    }

    /**
     * 删除会话(逻辑删除)
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteSession(@PathVariable Long id) {
        log.info("删除会话，id={}", id);
        return sessionsService.deleteSession(id);
    }

    /**
     * 结束会话
     * @param session_id
     * @return
     */
    @PostMapping("/session/end/{session_id}")
    public Result endSession(@PathVariable Long session_id) {
        log.info("结束会话，session_id={}", session_id);
        return sessionsService.endSession(session_id);
    }
}
