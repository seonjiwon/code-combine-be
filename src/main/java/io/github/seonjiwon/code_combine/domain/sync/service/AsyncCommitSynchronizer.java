package io.github.seonjiwon.code_combine.domain.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncCommitSynchronizer {

    private final CommitSynchronizer commitSynchronizer;

    /**
     * 전체 커밋 동기화 - 비동기 실행
     */
    @Async("asyncExecutor")
    public void syncAllCommitsAsync(Long userId, Long repoId) {
        commitSynchronizer.syncAllCommits(userId, repoId);
    }
}