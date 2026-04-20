package com.itsnow;

import com.itsnow.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class CodeMindAgentServerApplicationTests {

    @Autowired
    private ChatService chatService;


    private Map<String, Object> testRequestBody;

    @BeforeEach
    void setUp() {
        testRequestBody = new HashMap<>();
        testRequestBody.put("question", "你好，请介绍一下自己");
    }

    private Map<String, Object> createTestRequest(String question) {
        Map<String, Object> request = new HashMap<>();
        request.put("question", question);
        return request;
    }

    /**
     * 测试 /chat 非流式接口正常响应
     */
    @Test
    void testChat() {
        String response = chatService.chat(testRequestBody).block();
        System.out.println(response);
    }

    @Test
    void testChatStream() {
        System.out.println("=== 开始流式请求 ===");
        long startTime = System.currentTimeMillis();

        chatService.chatStream(createTestRequest("展示项目文件列表"))
                .doOnNext(chunk -> {
                    long timestamp = System.currentTimeMillis() - startTime;
                    System.out.println("========================================");
                    System.out.println("[" + timestamp + "ms] 收到: " + chunk);
                    System.out.println("========================================");
                })
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("\n=== 流式输出完成，总耗时: " + duration + "ms ===");
                })
                .doOnError(error -> {
                    System.err.println("发生错误: " + error.getMessage());
                })
                .blockLast(Duration.ofSeconds(60));
    }

    @Test
    public void testHealth() {
        String response = String.valueOf(chatService.health().block());
        System.out.println(response);
    }


}
