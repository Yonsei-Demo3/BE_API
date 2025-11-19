package com.backend;

import com.backend.notification.NotificationMessage;
import com.backend.notification.NotificationPublisher;
import com.backend.notification.NotificationType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

    @Bean
    public CommandLineRunner testRedis(NotificationPublisher notificationPublisher) {
        return args -> {
            System.out.println("🚀 서버 시작되자마자 알림 발송 테스트!");
            notificationPublisher.publish(NotificationMessage.of(999L, NotificationType.QUESTION_FULL));
        };
    }

}
