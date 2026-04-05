package io.github.seonjiwon.code_combine.domain.repo.controller;

import io.github.seonjiwon.code_combine.domain.repo.dto.GitHubRepoResponse.RepoList;
import io.github.seonjiwon.code_combine.domain.repo.service.GitHubRepoService;
import io.github.seonjiwon.code_combine.domain.repo.dto.RepoRegisterRequest;
import io.github.seonjiwon.code_combine.domain.sync.service.AsyncCommitSynchronizer;
import io.github.seonjiwon.code_combine.domain.sync.service.CommitSynchronizer;
import io.github.seonjiwon.code_combine.global.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "레포 등록 API", description = "유저의 레포지토리를 등록합니다.")
public class RepoController {

    private final CommitSynchronizer commitSynchronizer;
    private final AsyncCommitSynchronizer asyncCommitSynchronizer;
    private final GitHubRepoService gitHubRepoService;

    @GetMapping("/repos")
    @Operation(
        summary = "사용자 Repository 조회",
        description = "사용자의 Repository 목록을 반환합니다."
    )
    public ResponseEntity<CustomResponse<RepoList>> getGitHubRepositories (
        @AuthenticationPrincipal Long userId
    ) {
        RepoList response = gitHubRepoService.getGithubRepositories(userId);
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    @PostMapping("/repo")
    @Operation(
        summary = "레포지토리 등록",
        description = "사용자의 GitHub 레포지토리를 등록하고 전체 커밋 초기 동기화를 시작합니다."
    )
    public ResponseEntity<CustomResponse<String>> registerRepository(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "사용자의 리포지토리 이름", example = "Java-Algorithm")
        @RequestBody RepoRegisterRequest repoRegisterRequest) {

        // 1. 레포지토리 등록
        Long repoId = commitSynchronizer.registerAndGetRepoId(userId, repoRegisterRequest);

        // 2. 처음 등록 된 레포의 경우 동기화
        if (repoId != null) {
            asyncCommitSynchronizer.syncAllCommitsAsync(userId, repoId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomResponse.onSuccess("레포지토리 등록 완료"));
    }

}
