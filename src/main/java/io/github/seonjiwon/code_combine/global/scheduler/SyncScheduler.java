package io.github.seonjiwon.code_combine.global.scheduler;

import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.repo.domain.SyncStatus;
import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.domain.repo.service.CommitSyncFacade;
import io.github.seonjiwon.code_combine.domain.repo.service.CommitSynchronizer;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
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
    private final CommitSyncFacade commitSyncFacade;
    private final RepoRepository repoRepository;
    private final CommitSynchronizer commitSynchronizer;

    private static final int MAX_RETRY = 3;

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    public void scheduleDailySync() {
        List<User> users = userRepository.findAll();
        log.info("=== 일일 동기화 시작 ===");

        for (User user : users) {
            try {
                commitSyncFacade.syncTodayCommits(user.getId());
            } catch (Exception e) {
                log.info("사용자 동기화 실패: userId={}, error={}", user.getId(), e.getMessage());
            }

        }

        log.info("=== 일일 자동 동기화 완료 ===");
    }

    /**
     * 6시간마다 - 동기화 실패 레포 재시도
     * FAILED 상태이면서 retryCount < MAX_RETRY 인 레포 대상
     */
    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    public void retryFailedSync() {
        List<Repo> failedRepos = repoRepository.findBySyncStatusAndRetryCountLessThan(
            SyncStatus.FAILED, MAX_RETRY);

        if (failedRepos.isEmpty()) {
            return;
        }

        log.info("실패 Repository 동기화 재시도 시작: {}건", failedRepos.size());

        for (Repo repo : failedRepos) {
            try {
                commitSynchronizer.syncAllCommits(repo.getUser().getId(), repo.getId());
            } catch (Exception e) {
                log.warn("재시도 실패: repoId={}, retryCount={}, error={}", repo.getId(),
                    repo.getRetryCount(), e.getMessage());
            }
        }

        log.info("실패 동기화 재시도 완료");
    }
}
