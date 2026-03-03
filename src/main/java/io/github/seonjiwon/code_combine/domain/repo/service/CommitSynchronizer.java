package io.github.seonjiwon.code_combine.domain.repo.service;

public interface CommitSynchronizer {
    void syncAllCommits(Long userId, Long repoId);
}
