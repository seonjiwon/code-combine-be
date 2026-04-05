package io.github.seonjiwon.code_combine.domain.solution.service;

import static io.github.seonjiwon.code_combine.domain.solution.dto.SolutionResponse.*;

import io.github.seonjiwon.code_combine.domain.solution.code.SolutionErrorCode;
import io.github.seonjiwon.code_combine.domain.solution.dto.SolutionResponse;
import io.github.seonjiwon.code_combine.domain.solution.dto.SolutionResponse.Submission;
import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import io.github.seonjiwon.code_combine.domain.solution.repository.SolutionRepository;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolutionQueryService {

    private final SolutionRepository solutionRepository;

    public Solution getById(Long solutionId) {
        return solutionRepository.findById(solutionId)
                                 .orElseThrow(() -> new CustomException(
                                     SolutionErrorCode.SOLUTION_NOT_FOUND));
    }

    /**
     * 특정 문제의 풀이 상세 조회
     */
    public Detail getDetailSolution(Long problemId) {
        // 1. 해당 문제의 모든 풀이를 가져옴
        List<Solution> solutions = solutionRepository.findAllByProblemIdWithUser(problemId);

        // 2. 제출 된 풀이를 기반으로 변환
        List<Submission> submissions = solutions.stream()
                                                .map(solution ->
                                                    Submission.builder()
                                                              .solutionId(solution.getId())
                                                              .username(
                                                                  solution.getUser().getUsername())
                                                              .language(solution.getLanguage())
                                                              .submissionCode(
                                                                  solution.getSourceCode())
                                                              .solveExplain(null)
                                                              .build())
                                                .toList();

        return Detail.builder()
                     .submissions(submissions)
                     .build();
    }
}