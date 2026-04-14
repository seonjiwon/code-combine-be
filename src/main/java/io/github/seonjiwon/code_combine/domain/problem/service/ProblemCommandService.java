package io.github.seonjiwon.code_combine.domain.problem.service;

import io.github.seonjiwon.code_combine.domain.problem.entity.Problem;
import io.github.seonjiwon.code_combine.domain.problem.entity.ProblemTier;
import io.github.seonjiwon.code_combine.domain.problem.repository.ProblemRepository;
import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProblemCommandService {

    private final ProblemRepository problemRepository;

    /**
     * 문제 조회 또는 생성 문제 번호로 문제를 조회하고, 없으면 새로 생성합니다.
     */
    public Problem findOrCreateProblem(ProblemInfo problemInfo) {
        return problemRepository.findByProblemNumber(problemInfo.problemNumber())
                                .orElseGet(() -> createProblem(problemInfo));
    }

    /**
     * 새로운 문제 생성
     */
    private Problem createProblem(ProblemInfo problemInfo) {
        Problem problem = Problem.builder()
                                 .problemNumber(problemInfo.problemNumber())
                                 .title(problemInfo.title())
                                 .problemUrl(buildProblemUrl(problemInfo.problemNumber()))
                                 .tier(problemInfo.tier() == null ? ProblemTier.UNRATED : problemInfo.tier())
                                 .build();

        Problem savedProblem = problemRepository.save(problem);
        log.debug("새로운 문제 생성: 번호={}, 제목={}", savedProblem.getProblemNumber(),
            savedProblem.getTitle());

        return savedProblem;
    }

    /**
     * 백준 문제 URL 생성
     */
    private String buildProblemUrl(int problemNumber) {
        return "https://www.acmicpc.net/problem/" + problemNumber;
    }
}
