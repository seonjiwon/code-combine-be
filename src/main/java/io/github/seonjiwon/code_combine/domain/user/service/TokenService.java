package io.github.seonjiwon.code_combine.domain.user.service;

import io.github.seonjiwon.code_combine.domain.user.code.UserErrorCode;
import io.github.seonjiwon.code_combine.domain.user.entity.GitToken;
import io.github.seonjiwon.code_combine.domain.user.entity.TokenStatus;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.repository.GitTokenRepository;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final GitTokenRepository gitTokenRepository;

    @Transactional(readOnly = true)
    public String getActiveToken(Long userId) {
        GitToken gitToken = gitTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVATED)
                                              .orElseThrow(() -> new CustomException(UserErrorCode.ACTIVE_TOKEN_NOT_FOUND));
        return gitToken.getToken();
    }

    // OAuth2 토큰 저장 또는 갱신
    public void saveOrUpdateToken(User user, String plainToken, LocalDateTime expiresAt) {
        // 1. 활성화된 토큰을 조회해서 토큰이 있으면 이를 비활성화
        gitTokenRepository.findByUserIdAndStatus(user.getId(), TokenStatus.ACTIVATED)
            .ifPresent(GitToken::deactivate);

        // 2. 새로운 토큰은 ACTIVATE 상태로 만듬
        GitToken newToken = GitToken.builder()
                                 .user(user)
                                 .token(plainToken)
                                 .issuedAt(LocalDateTime.now())
                                 .expiresAt(expiresAt)
                                 .status(TokenStatus.ACTIVATED)
                                 .build();
        gitTokenRepository.save(newToken);
        log.debug("사용자 {} 토큰 저장 완료", user.getGitId());
    }
}
