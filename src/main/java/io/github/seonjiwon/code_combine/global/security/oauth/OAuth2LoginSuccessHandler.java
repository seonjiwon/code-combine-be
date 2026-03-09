package io.github.seonjiwon.code_combine.global.security.oauth;

import io.github.seonjiwon.code_combine.domain.user.dto.OAuth2UserInfo;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import io.github.seonjiwon.code_combine.domain.user.service.TokenService;
import io.github.seonjiwon.code_combine.domain.user.service.UserCommandService;
import io.github.seonjiwon.code_combine.global.security.code.OAuth2ErrorCode;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import io.github.seonjiwon.code_combine.global.security.utils.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    @Value("${jwt.expiration}")
    private Long jwtExpiration;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
        throws IOException, ServletException {

        log.info("OAuth2 로그인 성공");

        // 1. GitHub 에서 받은 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String accessToken = extractAccessToken(authentication);

        log.info("사용자 정보 : {}", oAuth2User.getAttributes());
        OAuth2UserInfo userInfo = OAuth2UserInfo.from(
            oAuth2User.getAttributes()
        );

        // 2. User 조회 또는 생성
        User user = userCommandService.findOrCreateUser(userInfo);
        log.info("OAuth2 로그인 성공: gitId={}", user.getGitId());

        // 3. GitHub AccessToken 저장
        tokenService.saveOrUpdateToken(user, accessToken);

        // 4. jwt 토큰 생성
        String jwt = jwtProvider.createToken(user.getId());

        // 5. jwt 를 쿠키에 저장
        addJwtCookie(response, jwt);

        // 6. 리다이렉트
        String redirectUrl = frontendUrl + "/auth/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


    private String extractAccessToken(Authentication authentication) {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
            oauth2Token.getAuthorizedClientRegistrationId(), // "github"
            oauth2Token.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new CustomException(OAuth2ErrorCode.GET_TOKEN_ERROR);
        }

        return client.getAccessToken().getTokenValue();
    }

    private void addJwtCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                                              .httpOnly(true)
                                              .secure(false)
                                              .path("/")
                                              .maxAge(jwtExpiration / 1000)
                                              .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
