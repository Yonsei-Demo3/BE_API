package com.backend.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private static final String TOPIC_NOTIFICATION = "notification-topic";

    public void publish(NotificationMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(TOPIC_NOTIFICATION, jsonMessage);
            log.info("Published notification to topic: {}", jsonMessage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
