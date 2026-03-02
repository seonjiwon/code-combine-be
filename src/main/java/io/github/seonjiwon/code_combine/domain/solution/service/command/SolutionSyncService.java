package io.github.seonjiwon.code_combine.domain.solution.service.command;

import io.github.seonjiwon.code_combine.domain.problem.domain.Problem;
import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import io.github.seonjiwon.code_combine.domain.problem.service.ProblemCommandService;
import io.github.seonjiwon.code_combine.domain.solution.domain.Solution;
import io.github.seonjiwon.code_combine.domain.solution.repository.SolutionRepository;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import io.github.seonjiwon.code_combine.domain.user.repository.UserRepository;
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
    @Transactional(readOnly = true)
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
                                    .language(problemInfo.getLanguage())
                                    .sourceCode(sourceCode)
                                    .commitSha(commitSha)
                                    .filePath(sourceCodePath)
                                    .solvedAt(commitDetail.commitDate())
                                    .build();

        solutionRepository.save(solution);
        log.info("풀이 저장 완료: 문제 번호={}, 사용자={}", problem.getProblemNumber(), user.getUsername());
    }
}
