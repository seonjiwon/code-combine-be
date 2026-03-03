package io.github.seonjiwon.code_combine.domain.repo.service;

import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.repo.domain.SyncStatus;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegisterRequest;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegistrationResult;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import io.github.seonjiwon.code_combine.domain.user.service.TokenService;
import io.github.seonjiwon.code_combine.domain.user.service.UserQueryService;
import io.github.seonjiwon.code_combine.global.infra.github.GitHubFetcher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommitSyncFacade {
    private final RepoCommandService repoCommandService;
    private final TokenService tokenService;
    private final UserQueryService userQueryService;
    private final RepoQueryService repoQueryService;
    private final GitHubFetcher fetcher;

    private final SingleCommitSynchronizer singleCommitSynchronizer;
    private final CommitSynchronizer commitSynchronizer;

    /**
     * 레포지토리 등록 + 전체 커밋 동기화
     */
    public void registerAndSync(Long userId, RepoRegisterRequest request) {
        RepoRegistrationResult result = repoCommandService.registerRepository(userId, request);

        // 이미 레포지토리 등록이 된 경우
        if (result == null) {
            return;
        }

        // 동기화가 완료 된 경우
        if (result.repo().getSyncStatus() == SyncStatus.COMPLETED) {
            log.info("레포 {}는 이미 동기화 완료 상태입니다.", result.repo().getName());
            return;
        }

        log.info("초기 동기화 시작: userId={}, repo={}", userId, result.repo().getName());
        commitSynchronizer.syncAllCommits(userId, result.repo().getId());
    }



    /**
     * 오늘 커밋 동기화 - 일일 스케줄러에서 호출
     */
    public void syncTodayCommits(Long userId) {
        User user = userQueryService.getById(userId);
        Repo repo = repoQueryService.getByUserId(userId);

        String owner = user.getUsername();
        String repoName = repo.getName();
        String token = tokenService.getActiveToken(userId);

        log.info("오늘 커밋 동기화 시작: owner={}", owner);

        List<String> commitShas = fetcher.fetchTodayCommitShas(token, owner, repoName);

        commitShas.forEach(sha -> {
            try {
                singleCommitSynchronizer.syncSingleCommit(user, token, owner, repoName, sha);
            } catch (Exception e) {
                log.info("커밋 동기화 실패: sha={}, error={}", sha, e.getMessage());
            }
        });

        log.info("오늘 커밋 동기화 완료");
    }


}
