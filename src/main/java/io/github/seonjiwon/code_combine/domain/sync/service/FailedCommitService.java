package io.github.seonjiwon.code_combine.domain.sync.service;

import io.github.seonjiwon.code_combine.domain.sync.entity.FailedCommit;
import io.github.seonjiwon.code_combine.domain.repo.entity.Repo;
import io.github.seonjiwon.code_combine.domain.sync.repository.FailedCommitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FailedCommitService {

    private static final int MAX_RETRY = 3;

    private final FailedCommitRepository failedCommitRepository;

    public void saveFailedCommit(Repo repo, String commitSha, Exception e) {
        FailedCommit failedCommit = FailedCommit.builder()
                                                .repo(repo)
                                                .commitSha(commitSha)
                                                .build();

        failedCommitRepository.save(failedCommit);
        log.warn("실패 커밋 저장: sha={}, error={}", commitSha, e.getMessage());
    }

    public List<FailedCommit> findRetryableCommits() {
        return failedCommitRepository.findByRetryCountLessThan(MAX_RETRY);
    }

    public void delete(FailedCommit failedCommit) {
        failedCommitRepository.delete(failedCommit);
    }
}