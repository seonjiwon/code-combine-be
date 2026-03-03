package io.github.seonjiwon.code_combine.domain.repo.service;

import io.github.seonjiwon.code_combine.domain.repo.code.RepoErrorCode;
import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepoQueryService {

    private final RepoRepository repoRepository;

    public Repo getById(Long repoId) {
        return repoRepository.findById(repoId)
                             .orElseThrow(() -> new CustomException(RepoErrorCode.REPO_NOT_FOUND));
    }

    public Repo getByUserId(Long userId) {
        return repoRepository.findByUserId(userId)
                             .orElseThrow(() -> new CustomException(RepoErrorCode.REPO_NOT_FOUND));
    }
}
