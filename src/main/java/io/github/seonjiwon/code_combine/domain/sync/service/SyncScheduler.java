package io.github.seonjiwon.code_combine.domain.sync.service;

import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {
    private final UserRepository userRepository;
    private final CommitSynchronizer commitSynchronizer;

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    public void scheduleDailySync() {
        List<User> users = userRepository.findAll();
        log.info("=== 일일 동기화 시작 ===");

        for (User user : users) {
            try {
                commitSynchronizer.syncTodayCommits(user.getId());
            } catch (Exception e) {
                log.warn("사용자 동기화 실패: userId={}", user.getId(), e);
            }
        }

        log.info("=== 일일 동기화 완료 ===");

        commitSynchronizer.retryFailedCommits();
    }
}