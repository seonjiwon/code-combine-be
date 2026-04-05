package io.github.seonjiwon.code_combine.domain.problem.controller;

import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemsResponse.ProblemSolveList;
import io.github.seonjiwon.code_combine.domain.problem.service.ProblemQueryService;
import io.github.seonjiwon.code_combine.global.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "문제 정보 API", description = "유저가 해결한 문제 목록을 가져옵니다.")
public class ProblemController {

    private final ProblemQueryService problemQueryService;

    @GetMapping("/problems")
    @Operation(
        summary = "문제 리스트 받아오기",
        description = "커서 기반으로 문제 리스트를 받아옵니다. 첫 페이지 요청 시 cursor 없이 호출."
    )
    public ResponseEntity<CustomResponse<ProblemSolveList>> getProblemList(
        @Parameter(description = "이전 응답의 cursor 값. 첫 페이지는 생략 가능", example = "10")
        @RequestParam(value = "cursor", required = false) String cursor
    ) {
        ProblemSolveList problemSolveList = problemQueryService.getProblemList(cursor);
        return ResponseEntity.ok(CustomResponse.onSuccess(problemSolveList));
    }

    @GetMapping("/problems/search")
    @Operation(
        summary = "문제 검색",
        description = "숫자 입력 시 문제 번호 완전 일치, 문자 입력 시 제목 전방 일치로 검색합니다."
    )
    public ResponseEntity<CustomResponse<ProblemSolveList>> searchProblems(
        @Parameter(description = "검색 키워드 (문제 번호 또는 제목)", example = "두 수")
        @RequestParam String keyword
    ) {
        ProblemSolveList result = problemQueryService.searchProblems(keyword);
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }
}
