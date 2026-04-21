package com.itsnow.controller;

import com.itsnow.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author itsnow
 * @date 2026/4/20
 */
@RestController
@Slf4j
public class HealthController {

    @Autowired
    ChatService chatService;

    /**
     * 健康检查
     * @return
     */
    @GetMapping("/health")
    public Mono<String> health(){
        log.info("健康检查");
        return chatService.health();
    }
}
