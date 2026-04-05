package io.github.seonjiwon.code_combine.domain.review.service;

import io.github.seonjiwon.code_combine.domain.review.entity.Review;
import io.github.seonjiwon.code_combine.domain.review.dto.ReviewRequest;
import io.github.seonjiwon.code_combine.domain.review.repository.ReviewRepository;
import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import io.github.seonjiwon.code_combine.domain.solution.service.SolutionQueryService;
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
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;

    private final SolutionQueryService solutionQueryService;
    private final UserRepository userRepository;

    public void createReview(Long userId, Long solutionId, ReviewRequest request) {
        Solution solution = solutionQueryService.getById(solutionId);
        User reviewer = userRepository.findById(userId)
                                      .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Review review = Review.builder()
            .solution(solution)
            .reviewer(reviewer)
            .lineNumber(request.lineNumber())
            .content(request.content())
            .build();

        reviewRepository.save(review);
        log.debug("리뷰 등록 완료: userId={}, solutionId={}", userId, solutionId);
    }
}
