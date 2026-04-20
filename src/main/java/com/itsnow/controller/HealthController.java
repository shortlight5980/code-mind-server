package com.itsnow.controller;

import com.itsnow.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author itsnow
 * @date 2026/4/20
 */
@RestController
public class HealthController {

    @Autowired
    ChatService chatService;

    /**
     * 健康检查
     * @return
     */
    @GetMapping("/health")
    public Mono<String> health(){
        return chatService.health();
    }
}
