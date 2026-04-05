package io.github.seonjiwon.code_combine.domain.sync.service;

import io.github.seonjiwon.code_combine.domain.repo.code.RepoErrorCode;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegisterRequest;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegistrationResult;
import io.github.seonjiwon.code_combine.domain.sync.entity.FailedCommit;
import io.github.seonjiwon.code_combine.domain.repo.entity.Repo;
import io.github.seonjiwon.code_combine.domain.repo.entity.SyncStatus;
import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.domain.repo.service.RepoCommandService;
import io.github.seonjiwon.code_combine.domain.user.code.UserErrorCode;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.repository.UserRepository;
import io.github.seonjiwon.code_combine.domain.user.service.TokenService;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import io.github.seonjiwon.code_combine.global.infra.github.GitHubFetcher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitSynchronizer {

    private final TokenService tokenService;
    private final GitHubFetcher fetcher;

    private final RepoCommandService repoCommandService;
    private final FailedCommitService failedCommitService;
    private final SingleCommitSynchronizer singleCommitSynchronizer;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    /**
     * 레포지토리 등록 후 동기화가 필요한 경우 repoId를 반환
     */
    public Long registerAndGetRepoId(Long userId, RepoRegisterRequest request) {
        RepoRegistrationResult result = repoCommandService.registerRepository(userId, request);

        if (result == null) {
            return null;
        }

        if (result.repo().getSyncStatus() == SyncStatus.COMPLETED) {
            log.debug("레포 {}는 이미 동기화 완료 상태입니다.", result.repo().getName());
            return null;
        }

        log.info("초기 동기화 시작: userId={}, repo={}", userId, result.repo().getName());
        return result.repo().getId();
    }

    /**
     * 전체 커밋 동기화 - 레포 등록 시 호출. 모든 커밋 Sha 를 오래된 것 부터 조회하여 순차 동기화
     */
    public void syncAllCommits(Long userId, Long repoId) {
        // 1. 사용자, 레포 조회
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Repo repo = repoRepository.findById(repoId)
                        .orElseThrow(() -> new CustomException(RepoErrorCode.REPO_NOT_FOUND));

        String owner = user.getUsername();
        String repoName = repo.getName();

        log.info("전체 커밋 동기화 시작: owner={}, repo={}", owner, repoName);

        // 2. 동기화 상태 업데이트 NOT_STARTED -> IN_PROGRESS
        repoCommandService.startSync(repo);

        String token;
        List<String> allCommitShas;

        // 3. CommitSha 가져오기 (오래된 순)
        try {
            token = tokenService.getActiveToken(user.getId());
            allCommitShas = fetcher.fetchAllCommitShas(token, owner, repoName);
        } catch (Exception e) {
            repoCommandService.failSync(repo);
            log.error("동기화 준비 실패: owner={}, repo={}", owner, repoName, e);
            return;
        }

        // 4. 가져온 commit 동기화
        for (String sha : allCommitShas) {
            try {
                singleCommitSynchronizer.syncSingleCommit(user, token, owner, repoName, sha);
            } catch (Exception e) {
                failedCommitService.saveFailedCommit(repo, sha, e);
            }
        }

        // 5. 동기화 상태 업데이트 IN_PROGRESS -> COMPLETED
        repoCommandService.completeSync(repo);
        log.info("전체 커밋 동기화 완료: owner={}, repo={}", owner, repoName);
    }

    /**
     * 오늘 커밋 동기화 - 일일 스케줄러에서 호출
     */
    public void syncTodayCommits(Long userId) {
        // 1. 사용자 repo 정보 가져오기
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Repo repo = repoRepository.findByUserId(userId)
                        .orElseThrow(() -> new CustomException(RepoErrorCode.REPO_NOT_FOUND));

        String owner = user.getUsername();
        String repoName = repo.getName();
        String token = tokenService.getActiveToken(userId);

        log.info("오늘 커밋 동기화 시작: owner={}, repo={}", owner, repoName);

        // 2. 오늘자 커밋 조회
        List<String> commitShas = fetcher.fetchTodayCommitShas(token, owner, repoName);

        // 3. 커밋 동기화
        for (String sha : commitShas) {
            try {
                singleCommitSynchronizer.syncSingleCommit(user, token, owner, repoName, sha);
            } catch (Exception e) {
                failedCommitService.saveFailedCommit(repo, sha, e);
            }
        }

        log.info("오늘 커밋 동기화 완료: owner={}, repo={}", owner, repoName);
    }

    /**
     * 실패한 커밋 재시도 - 일일 동기화 후 호출
     */
    public void retryFailedCommits() {
        // 1. 재시도 해야하는 커밋 가져오기
        List<FailedCommit> failedCommits = failedCommitService.findRetryableCommits();

        if (failedCommits.isEmpty()) {
            return;
        }

        log.info("실패 커밋 재시도 시작: {}건", failedCommits.size());

        for (FailedCommit failedCommit : failedCommits) {
            // 1. 레포 가져오기
            Repo repo = failedCommit.getRepo();
            User user = repo.getUser();
            String token = tokenService.getActiveToken(user.getId());

            try {
                // 2. 커밋 동기화
                singleCommitSynchronizer.syncSingleCommit(
                    user, token, user.getUsername(), repo.getName(), failedCommit.getCommitSha()
                );

                // 3. 완료한 커밋 삭제
                failedCommitService.delete(failedCommit);
                log.info("실패 커밋 재시도 성공: sha={}", failedCommit.getCommitSha());
            } catch (Exception e) {
                // 재시도 카운트 증가
                failedCommit.incrementRetryCount();
                log.warn("실패 커밋 재시도 실패: sha={}, retryCount={}",
                    failedCommit.getCommitSha(), failedCommit.getRetryCount(), e);
            }
        }

        log.info("실패 커밋 재시도 완료");
    }
}