package io.github.seonjiwon.code_combine.domain.review.controller;

import io.github.seonjiwon.code_combine.domain.review.dto.ReviewRequest;
import io.github.seonjiwon.code_combine.domain.review.dto.ReviewResponse;
import io.github.seonjiwon.code_combine.domain.review.service.ReviewCommandService;
import io.github.seonjiwon.code_combine.domain.review.service.ReviewQueryService;
import io.github.seonjiwon.code_combine.global.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/solutions/{solutionId}")
@Tag(name = "리뷰 API", description = "풀이 코드에 대한 라인별 리뷰를 등록하고 조회합니다.")
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("/reviews")
    @Operation(
        summary = "리뷰 등록",
        description = "풀이의 코드 라인에 리뷰를 등록합니다."
    )
    public ResponseEntity<CustomResponse<String>> createReview(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "풀이 ID", example = "1")
        @PathVariable Long solutionId,
        @RequestBody ReviewRequest request
    ) {
        reviewCommandService.createReview(userId, solutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomResponse.onSuccess("리뷰를 등록했습니다."));
    }

    @GetMapping("/reviews")
    @Operation(
        summary = "리뷰 목록 조회",
        description = "풀이에 달린 모든 라인별 리뷰를 조회합니다."
    )
    public ResponseEntity<CustomResponse<List<ReviewResponse>>> getReview(
        @Parameter(description = "풀이 ID", example = "1")
        @PathVariable Long solutionId
    ) {
        List<ReviewResponse> review = reviewQueryService.getReview(solutionId);
        return ResponseEntity.ok(CustomResponse.onSuccess(review));
    }
}
