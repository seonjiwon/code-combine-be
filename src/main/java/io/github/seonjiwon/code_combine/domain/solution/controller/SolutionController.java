package io.github.seonjiwon.code_combine.domain.solution.controller;

import io.github.seonjiwon.code_combine.domain.repo.service.CommitSyncFacade;
import io.github.seonjiwon.code_combine.domain.solution.dto.DashboardResponse;
import io.github.seonjiwon.code_combine.domain.solution.dto.SolutionResponse;
import io.github.seonjiwon.code_combine.domain.solution.service.query.CommitQueryService;
import io.github.seonjiwon.code_combine.domain.solution.service.query.SolutionQueryService;
import io.github.seonjiwon.code_combine.global.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "풀이 API", description = "풀이 동기화 및 조회 기능을 제공합니다.")
public class SolutionController {

    private final CommitSyncFacade commitSyncFacade;
    private final SolutionQueryService solutionQueryService;
    private final CommitQueryService commitQueryService;

    /**
     * 오늘의 커밋 동기화
     */
    @PostMapping("/sync")
    @Operation(
        summary = "오늘 커밋 동기화",
        description = "로그인한 사용자의 오늘 GitHub 커밋을 수동으로 동기화합니다."
    )
    public CustomResponse<String> syncTodayCommits(
        @AuthenticationPrincipal Long userId
    ) {
        commitSyncFacade.syncTodayCommits(userId);
        return CustomResponse.onSuccess("동기화 성공!");
    }

    /**
     * 주간 커밋 통계 조회
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "주간 커밋 통계 조회",
        description = "이번 주 일~토 기준으로 날짜별 사용자 커밋 수를 조회합니다."
    )
    public CustomResponse<DashboardResponse.WeeklyCommitInfo> getWeeklyCommitInfo() {
        DashboardResponse.WeeklyCommitInfo weeklyCommitInfo = commitQueryService.getWeeklyCommitInfo();
        return CustomResponse.onSuccess(weeklyCommitInfo);
    }

    /**
     * 특정 문제의 풀이 상세 조회
     */
    @GetMapping("/problems/{problemId}/solutions")
    @Operation(
        summary = "문제별 풀이 목록 조회",
        description = "문제에 대한 모든 사용자의 풀이 코드를 조회합니다."
    )
    public CustomResponse<SolutionResponse.Detail> getSolutionDetail(
        @Parameter(description = "문제 ID", example = "1")
        @PathVariable Long problemId
    ) {
        SolutionResponse.Detail detailSolution = solutionQueryService.getDetailSolution(problemId);
        return CustomResponse.onSuccess(detailSolution);
    }
}
