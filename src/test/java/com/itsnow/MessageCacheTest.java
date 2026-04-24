package com.itsnow;

import com.itsnow.service.ChatService;
import com.itsnow.service.SessionsService;
import com.itsnow.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.itsnow.constant.RedisConstants.MESSAGES_HISTORY_KEY;

/**
 * @author itsnow
 * @date 2026/4/23
 */
@SpringBootTest
public class MessageCacheTest {
    @Autowired
    ChatServiceImpl chatServiceImpl;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
//        chatServiceImpl.saveMessage("{\"type\": \"human\", \"data\": {\"content\": \"test\", \"additional_kwargs\": {}, \"response_metadata\": {}, \"type\": \"human\", \"name\": null, \"id\": \"a84c6af4-19bb-4c5c-80b7-7df84ddafdd8\"}}", 1L);

        Long sessionId = 1L;
        String content = "{\"type\": \"human\", \"data\": {\"content\": \"test\", \"additional_kwargs\": {}, \"response_metadata\": {}, \"type\": \"human\", \"name\": null, \"id\": \"a84c6af4-19bb-4c5c-80b7-7df84ddafdd8\"}}";



    }
}
