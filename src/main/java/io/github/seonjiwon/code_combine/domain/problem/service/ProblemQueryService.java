package io.github.seonjiwon.code_combine.domain.problem.service;

import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemsResponse.ProblemSolveList;
import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemsResponse.SolveInfo;
import io.github.seonjiwon.code_combine.domain.problem.entity.Problem;
import io.github.seonjiwon.code_combine.domain.problem.repository.ProblemRepository;
import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import io.github.seonjiwon.code_combine.domain.solution.repository.SolutionRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemQueryService {

    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;
    private static final int PAGE_SIZE = 10;

    /**
     * 문제 목록 조회 (커서 기반 페이지네이션)
     */
    public ProblemSolveList getProblemList(String cursor) {
        log.debug("문제 목록 조회: cursor={}", cursor);

        // 1. 문제 조회 (+1개 더 조회하여 hasNext 판단)
        List<Problem> problems = fetchProblems(cursor, PAGE_SIZE + 1);

        // 2. 페이지네이션 처리
        boolean hasNext = problems.size() > PAGE_SIZE;
        if (hasNext) {
            problems.remove(problems.size() - 1);
        }
        String nextCursor = generateNextCursor(problems, hasNext);

        // 3. 문제별 풀이 조회
        Map<Long, List<Solution>> solverMap = groupSolversByProblem(problems);

        // 4. 응답 생성
        List<SolveInfo> solveInfos = buildSolveInfoList(problems, solverMap);

        return ProblemSolveList.from(solveInfos, nextCursor);
    }

    /**
     * 문제 검색
     */
    public ProblemSolveList searchProblems(String keyword) {
        // 1. 숫자면 숫자 문제 숫자로 탐색 아니면 이름으로 탐색
        List<Problem> problems = keyword.matches("\\d+")
            ? problemRepository.findByProblemNumberStartingWith(keyword)
            : problemRepository.findByTitleStartingWith(keyword);

        // 2. 문제 번호 별로 그룹핑
        Map<Long, List<Solution>> solverMap = groupSolversByProblem(problems);

        // 3. 올바른 형태로 전환
        List<SolveInfo> solveInfos = buildSolveInfoList(problems, solverMap);
        return ProblemSolveList.from(solveInfos, null);
    }

    /**
     * 커서 기반으로 문제 조회
     */
    private List<Problem> fetchProblems(String cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        if (cursor == null || cursor.isEmpty()) {
            return problemRepository.findProblems(pageable);
        }
        return problemRepository.findProblems(Long.parseLong(cursor), pageable);
    }

    /**
     * 다음 커서 생성
     */
    private String generateNextCursor(List<Problem> problems, boolean hasNext) {
        if (!hasNext || problems.isEmpty()) {
            return null;
        }
        return String.valueOf(problems.get(problems.size() - 1).getId());
    }

    /**
     * 문제별 풀이 조회 및 그룹핑
     */
    private Map<Long, List<Solution>> groupSolversByProblem(List<Problem> problems) {
        if (problems.isEmpty()) {
            return Map.of();
        }

        // 1. 문제의 Id 가져오기
        List<Long> problemIds = problems.stream()
                                        .map(Problem::getId)
                                        .toList();

        log.debug("문제를 풀이 조회: problemIds 수={}", problemIds.size());
        // 2. 문제 Id로 풀이 조회
        List<Solution> solutions = solutionRepository.findAllByProblemIdsWithUser(problemIds);
        log.debug("조회된 풀이 수: {}", solutions.size());

        // 3. 문제 Id 로 그룹핑
        return solutions.stream()
                        .collect(Collectors.groupingBy(
                            solution -> solution.getProblem().getId()));
    }

    /**
     * SolveInfo 리스트 생성
     */
    private List<SolveInfo> buildSolveInfoList(List<Problem> problems,
                                               Map<Long, List<Solution>> solverMap) {
        return problems.stream()
                       .map(problem -> SolveInfo.from(
                           problem,
                           solverMap.getOrDefault(problem.getId(), List.of())
                       ))
                       .toList();
    }
}