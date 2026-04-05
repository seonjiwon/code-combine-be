package io.github.seonjiwon.code_combine.domain.user.controller;

import io.github.seonjiwon.code_combine.domain.user.dto.LoginSuccessResponse;
import io.github.seonjiwon.code_combine.domain.user.service.UserCommandService;
import io.github.seonjiwon.code_combine.domain.user.service.UserQueryService;
import io.github.seonjiwon.code_combine.global.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "사용자 API", description = "로그인한 사용자 정보를 조회합니다.")
public class UserController {

    private final UserQueryService userQueryService;

    @GetMapping("/me")
    @Operation(
        summary = "내 정보 조회",
        description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 정보를 반환합니다."
    )
    public ResponseEntity<CustomResponse<LoginSuccessResponse>> getLoginSuccessUserInfo(
        @AuthenticationPrincipal Long userId) {

        LoginSuccessResponse loginSuccessUserInfo
            = userQueryService.getLoginSuccessUserInfo(userId);
        return ResponseEntity.ok(CustomResponse.onSuccess(loginSuccessUserInfo));
    }
}
