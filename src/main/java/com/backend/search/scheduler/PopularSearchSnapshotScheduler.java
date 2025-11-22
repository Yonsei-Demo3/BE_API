package com.backend.search.scheduler;

import com.backend.search.service.PopularSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularSearchSnapshotScheduler {

    private final PopularSearchService popularSearchService;

    /**
     * 매 정각마다 현재 인기 검색어 TOP 50 스냅샷 저장
     *  - 필요에 따라 개수 조절 가능
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul") // 초 분 시 일 월 요일 (매 시간 00분: 1시간 간격)
    public void snapshotHourly() {
        log.info("[PopularSearch] Taking hourly snapshot...");
        popularSearchService.snapshotTopKeywords(50);
    }
}
