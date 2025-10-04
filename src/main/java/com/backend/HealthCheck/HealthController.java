package com.backend.HealthCheck;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "헬스체크")
@RestController
public class HealthController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 작동하는지 확인합니다.")
    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> healthz() {
        Map<String, Object> body = Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity.ok()
                // 프록시/클라이언트 캐싱 방지
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .header("Pragma", "no-cache")
                .body(body);
    }
}
