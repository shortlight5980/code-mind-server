package com.itsnow.controller;

import com.itsnow.domain.pojo.Result;
import com.itsnow.service.ChatService;
import com.itsnow.service.MessagesService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private MessagesService messagesService;

    /**
     * 非流式聊天接口
     * @param requestBody 请求体，包含 question 字段
     * @return 响应结果
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> chat(@RequestBody Map<String, Object> requestBody) {
        log.info("接收到非流式请求：{}",requestBody);
        return chatService.chat(requestBody);
    }

    /**
     * 流式聊天接口
     * @param requestBody 请求体，包含 question 字段
     * @return 流式响应
     */
    @PostMapping(value = "/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody Map<String, Object> requestBody) {
        log.info("接收到流式请求：{}",requestBody);
        return chatService.chatStream(requestBody);
    }

    /**
     * 获取历史消息
     * @param sessionId
     * @return
     */
    @GetMapping("/history")
    public Result getHistoryMessages(Long sessionId) {
        log.info("获取历史消息：{}",sessionId);
        return messagesService.getHistoryMessages(sessionId);
    }



}
