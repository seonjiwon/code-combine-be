package io.github.seonjiwon.code_combine.domain.user.service;

import io.github.seonjiwon.code_combine.domain.repo.repository.RepoRepository;
import io.github.seonjiwon.code_combine.domain.user.code.UserErrorCode;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.dto.LoginSuccessResponse;
import io.github.seonjiwon.code_combine.domain.user.repository.UserRepository;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    /**
     * 로그인 성공 후 사용자 정보 조회
     */
    public LoginSuccessResponse getLoginSuccessUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                                 .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        boolean hasRepo = repoRepository.existsByUserId(userId);

        return LoginSuccessResponse.builder()
                                   .userId(user.getId())
                                   .username(user.getUsername())
                                   .avatarUrl(user.getAvatarUrl())
                                   .hasRepo(hasRepo)
                                   .build();
    }
}
