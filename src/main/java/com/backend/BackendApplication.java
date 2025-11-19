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
            // 별도의 스레드에서 실행 (메인 서버 부팅을 방해하지 않기 위함)
            new Thread(() -> {
                try {
                    System.out.println("⏳ 서버 시작! 10초 뒤에 알림 발송 예정...");

                    // 10초 대기 (10000ms)
                    Thread.sleep(10000);

                    System.out.println("🚀 10초 경과! 알림 발송!");
                    notificationPublisher.publish(NotificationMessage.of(1L, NotificationType.QUESTION_FULL));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start(); // 스레드 시작
        };
    }

}
