package com.itsnow;

import com.itsnow.domain.pojo.Messages;
import com.itsnow.domain.pojo.Result;
import com.itsnow.service.ChatService;
import com.itsnow.service.MessagesService;
import com.itsnow.service.SessionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CodeMindAgentServerApplicationTests {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionsService sessionsService;

    private Map<String, Object> testRequestBody;
    @Autowired
    private MessagesService messagesService;

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
        String response = chatService.chat(createTestRequest("展示项目文件列表")).block();
        System.out.println(response);
    }

    @Test
    void testChatStream() {
        System.out.println("=== 开始流式请求 ===");
        long startTime = System.currentTimeMillis();

        chatService.chatStream(createTestRequest("我问过你哪些问题？"))
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

    @Test
    public void testGetMessagesBySessionId() {
        List<Messages> messages = messagesService.getMessagesBySessionId(1L);
        List<Map<String, String>> history = new ArrayList<>();

        for (Messages msg : messages) {
            Map<String, String> map = new HashMap<>();
            map.put("content", msg.getContent());
            history.add(map);
        }

        System.out.println(history);
    }

    @Test
    public void testGetSessionHistory() {
        Result res = sessionsService.getByUserId(1L);
        System.out.println(res);
    }

    @Test
    public void testGetHistoryMessages() {
        Result res = messagesService.getHistoryMessages(1L);
        System.out.println(res);
    }


}
