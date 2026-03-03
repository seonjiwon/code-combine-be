package io.github.seonjiwon.code_combine.domain.repo.service;

import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import io.github.seonjiwon.code_combine.domain.user.service.TokenService;
import io.github.seonjiwon.code_combine.domain.user.service.UserQueryService;
import io.github.seonjiwon.code_combine.global.infra.github.GitHubFetcher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//@Service
@RequiredArgsConstructor
@Slf4j
public class SyncCommitSynchronizer implements CommitSynchronizer{

    private final TokenService tokenService;
    private final GitHubFetcher fetcher;
    private final RepoCommandService repoCommandService;
    private final SingleCommitSynchronizer singleCommitSynchronizer;
    private final UserQueryService userQueryService;
    private final RepoQueryService repoQueryService;

    /**
     * 전체 커밋 동기화 - 레포 등 시 호출 모드 커밋 Sha 를 오래된 것 부터 조회하여 순차 동기화
     */
    @Override
    public void syncAllCommits(Long userId, Long repoId) {
        User user = userQueryService.getById(userId);
        Repo repo = repoQueryService.getById(repoId);
        String owner = user.getUsername();
        String repoName = repo.getName();
        log.info("전체 커밋 동기화 시작: owner={}, repo={}", owner, repoName);

        repoCommandService.startSync(repo);

        String token;
        List<String> allCommitShas;

        try {
            token = tokenService.getActiveToken(user.getId());
            allCommitShas = fetcher.fetchAllCommitShas(token, owner, repoName);
        } catch (Exception e) {
            repoCommandService.failSync(repo);
            log.error("동기화 준비 실패: owner={}, error={}", owner, e.getMessage());
            return;
        }

        allCommitShas.forEach(sha -> {
            try {
                singleCommitSynchronizer.syncSingleCommit(user, token, owner, repoName, sha);
            } catch (Exception e) {
                log.warn("커밋 동기화 실패: sha={}, error={}", sha, e.getMessage());
            }
        });

        repoCommandService.completeSync(repo);
        log.info("전체 커밋 동기화 완료");
    }
}
