package io.github.seonjiwon.code_combine.domain.repo.service;

import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegisterRequest;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegistrationResult;
import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import io.github.seonjiwon.code_combine.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RepoCommandService {

    private final RepoRepository repoRepository;
    private final UserQueryService userQueryService;

    /**
     * 레포지토리 등록
     */
    public RepoRegistrationResult registerRepository(Long userId, RepoRegisterRequest request) {
        User user = userQueryService.getById(userId);

        if (repoRepository.existsByUserId(userId)) {
            log.info("사용자 {}의 Repository 는 이미 등록이 되어 있습니다.", userId);
            return null;
        }

        Repo repo = Repo.builder()
                        .user(user)
                        .name(request.getName())
                        .build();

        repoRepository.save(repo);
        log.info("Repository 등록 완료: userId={}, repoName={}", userId, repo.getName());

        return new RepoRegistrationResult(user, repo);
    }

    public void startSync(Repo repo) {
        repo.startSync();
        repoRepository.save(repo);
    }

    public void completeSync(Repo repo) {
        repo.completeSync();
        repoRepository.save(repo);
    }

    public void failSync(Repo repo) {
        repo.failSync();
        repoRepository.save(repo);
    }
}
