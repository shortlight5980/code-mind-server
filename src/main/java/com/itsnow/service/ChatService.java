package com.itsnow.service;

import com.itsnow.domain.pojo.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 非流式聊天
     * @param requestBody 请求体
     * @return 响应结果
     */
    Mono<String> chat(Map<String, Object> requestBody);

    /**
     * 流式聊天
     * @param requestBody 请求体
     * @return 流式响应
     */
    Flux<String> chatStream(Map<String, Object> requestBody);

    /**
     * 健康检查
     * @return
     */
    Mono<String> health();


}
