package io.github.seonjiwon.code_combine.domain.solution.service;

import io.github.seonjiwon.code_combine.domain.problem.entity.Problem;
import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import io.github.seonjiwon.code_combine.domain.problem.service.ProblemCommandService;
import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import io.github.seonjiwon.code_combine.domain.solution.repository.SolutionRepository;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.global.infra.github.dto.GitHubCommitDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SolutionSyncService {

    private final ProblemCommandService problemCommandService;
    private final SolutionRepository solutionRepository;

    /**
     * 커밋 중복 체크
     */
    public boolean existsByCommitSha(String commitSha) {
        return solutionRepository.existsByCommitSha(commitSha);
    }

    /**
     * 이미 조회된 커밋 데이터를 받아 Problem 조회/생성, Solution 저장
     */
    public void saveSolution(User user, ProblemInfo problemInfo, String sourceCode,
                             String commitSha, String sourceCodePath,
                             GitHubCommitDetail commitDetail) {
        // 1. Problem 조회 또느 생성
        Problem problem = problemCommandService.findOrCreateProblem(problemInfo);

        // 2. Solution 저장
        Solution solution = Solution.builder()
                                    .user(user)
                                    .problem(problem)
                                    .language(problemInfo.language())
                                    .sourceCode(sourceCode)
                                    .commitSha(commitSha)
                                    .filePath(sourceCodePath)
                                    .solvedAt(commitDetail.commitDate())
                                    .build();

        solutionRepository.save(solution);
        log.debug("풀이 저장 완료: 문제 번호={}, 사용자={}", problem.getProblemNumber(), user.getUsername());
    }
}
