package io.github.seonjiwon.code_combine.domain.repo.service;

import io.github.seonjiwon.code_combine.domain.repo.entity.Repo;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegisterRequest;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegistrationResult;
import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.domain.user.code.UserErrorCode;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.repository.UserRepository;
import io.github.seonjiwon.code_combine.domain.user.service.UserQueryService;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
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
    private final UserRepository userRepository;

    /**
     * 레포지토리 등록
     */
    public RepoRegistrationResult registerRepository(Long userId, RepoRegisterRequest request) {
         // 1. 사용자 가져오기
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 이미 존재하는 repo면 Pass
        if (repoRepository.existsByUserId(userId)) {
            log.debug("사용자 {}의 Repository 는 이미 등록이 되어 있습니다.", userId);
            return null;
        }

        // 3. 레포 생성
        Repo repo = Repo.builder()
                        .user(user)
                        .name(request.getName())
                        .build();

        // 4. 레포 저장
        repoRepository.save(repo);
        log.debug("Repository 등록 완료: userId={}, repoName={}", userId, repo.getName());

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
