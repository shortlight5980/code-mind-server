package com.itsnow.controller;

import com.itsnow.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 聊天接口控制器
 * @author itsnow
 * @date 2026/4/18
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 非流式聊天接口
     * @param requestBody 请求体，包含 question 字段
     * @return 响应结果
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> chat(@RequestBody Map<String, Object> requestBody) {
        return chatService.chat(requestBody);
    }

    /**
     * 流式聊天接口
     * @param requestBody 请求体，包含 question 字段
     * @return 流式响应
     */
    @PostMapping(value = "/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody Map<String, Object> requestBody) {
        return chatService.chatStream(requestBody);
    }


}
