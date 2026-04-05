package io.github.seonjiwon.code_combine.global.security.oauth;

import io.github.seonjiwon.code_combine.domain.user.dto.OAuth2UserInfo;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.domain.user.service.TokenService;
import io.github.seonjiwon.code_combine.domain.user.service.UserCommandService;
import io.github.seonjiwon.code_combine.global.security.code.OAuth2ErrorCode;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import io.github.seonjiwon.code_combine.global.security.utils.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final UserCommandService userCommandService;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;


    @Value("${frontend.url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
        throws IOException, ServletException {

        // 1. GitHub OAuth2 토큰 추출
        OAuth2AccessToken oAuth2AccessToken = extractOAuth2AccessToken(authentication);
        String accessToken = oAuth2AccessToken.getTokenValue();
        Instant expiresAt = oAuth2AccessToken.getExpiresAt();

        if (expiresAt == null) {
            expiresAt = Instant.now().plusSeconds(8 * 60 * 60); // 기본 8시간
        }

        // 2. GitHub 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.debug("OAuth2 사용자 정보: {}", oAuth2User.getAttributes());
        OAuth2UserInfo userInfo = OAuth2UserInfo.from(oAuth2User.getAttributes());

        // 3. User 조회 또는 생성
        User user = userCommandService.findOrCreateUser(userInfo);
        log.info("OAuth2 로그인 성공: gitId={}", user.getGitId());

        // 4. GitHub AccessToken 저장 - 만료 시간을 GitHub 토큰 기준으로 설정
        LocalDateTime tokenExpiresAt = LocalDateTime.ofInstant(expiresAt, ZoneId.of("Asia/Seoul"));
        tokenService.saveOrUpdateToken(user, accessToken, tokenExpiresAt);

        // 5. JWT 토큰 생성 - GitHub 토큰 만료 시간과 동일하게 설정
        String jwt = jwtProvider.createToken(user.getId(), Date.from(expiresAt));

        // 6. JWT 를 쿠키에 저장
        long cookieMaxAge = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        addJwtCookie(response, jwt, cookieMaxAge);

        // 7. 리다이렉트
        String redirectUrl = frontendUrl + "/auth/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


    private OAuth2AccessToken extractOAuth2AccessToken(Authentication authentication) {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
            oauth2Token.getAuthorizedClientRegistrationId(),
            oauth2Token.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new CustomException(OAuth2ErrorCode.GET_TOKEN_ERROR);
        }

        return client.getAccessToken();
    }

    private void addJwtCookie(HttpServletResponse response, String jwt, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                                              .httpOnly(true)
                                              .secure(true)
                                              .path("/")
                                              .maxAge(maxAgeSeconds)
                                              .sameSite("None")
                                              .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
