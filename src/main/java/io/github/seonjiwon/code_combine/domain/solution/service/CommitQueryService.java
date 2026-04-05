package io.github.seonjiwon.code_combine.domain.solution.service;

import io.github.seonjiwon.code_combine.domain.solution.dto.DashboardResponse.SolvedProblemInfo;
import io.github.seonjiwon.code_combine.domain.solution.dto.DashboardResponse.UserCommit;
import io.github.seonjiwon.code_combine.domain.solution.dto.DashboardResponse.WeeklyCommitInfo;
import io.github.seonjiwon.code_combine.domain.solution.dto.DashboardResponse.WeeklyState;
import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import io.github.seonjiwon.code_combine.domain.solution.repository.SolutionRepository;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommitQueryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SolutionRepository solutionRepository;

    /**
     * 주간 커밋 통계 조회
     * - 기존: DailyUserCommitCountProjection (GROUP BY + COUNT 집계 프로젝션)
     * - 변경: fetch join으로 Solution + User를 가져와서 Java에서 집계
     */
    public WeeklyCommitInfo getWeeklyCommitInfo(LocalDate startDate) {
        // 1. 이번 주 날짜 범위 계산
        LocalDate endDate = startDate.plusDays(7);

        LocalDateTime start = startDate.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime end = endDate.atStartOfDay(KST).toLocalDateTime();

        // 2. 기간 내 풀이를 User와 함께 fetch join으로 조회
        List<Solution> solutions = solutionRepository.findAllByPeriodWithUser(start, end);

        // 3. 날짜별로 그룹핑
        Map<LocalDate, List<Solution>> grouped = solutions.stream()
                                                          .collect(Collectors.groupingBy(
                                                              solution -> solution.getSolvedAt().toLocalDate()));

        // 4. 응답 DTO 변환
        return convertWeeklyStats(startDate, grouped);
    }

    /**
     * 집계 데이터를 응답 DTO로 변환
     */
    private WeeklyCommitInfo convertWeeklyStats(LocalDate startDate,
                                                Map<LocalDate, List<Solution>> grouped) {
        List<WeeklyState> weeklyStats = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            List<Solution> dailySolutions = grouped.getOrDefault(date, List.of());

            // 유저별 커밋 수 집계 (기존 DB GROUP BY를 Java로 대체)
            List<UserCommit> userCommits = dailySolutions.stream()
                                                         .collect(Collectors.groupingBy(
                                                             solution -> solution.getUser().getId(),
                                                             Collectors.counting()))
                                                         .entrySet().stream()
                                                         .map(entry -> {
                                                             User user = dailySolutions.stream()
                                                                                       .filter(s -> s.getUser().getId().equals(entry.getKey()))
                                                                                       .findFirst()
                                                                                       .get()
                                                                                       .getUser();

                                                             // 해당 유저의 당일 풀이에서 문제 정보 추출
                                                             List<SolvedProblemInfo> solvedProblems = dailySolutions.stream()
                                                                                                                    .filter(s -> s.getUser().getId().equals(entry.getKey()))
                                                                                                                    .map(s -> SolvedProblemInfo.builder()
                                                                                                                                               .problemId(s.getProblem().getId())
                                                                                                                                               .problemName(s.getProblem().getTitle())
                                                                                                                                               .tier(s.getProblem().getTier())
                                                                                                                                               .build())
                                                                                                                    .toList();

                                                             return UserCommit.builder()
                                                                              .userId(user.getId())
                                                                              .username(user.getUsername())
                                                                              .avatarUrl(user.getAvatarUrl())
                                                                              .commitCount(entry.getValue().intValue())
                                                                              .solvedProblems(solvedProblems)
                                                                              .build();
                                                         })
                                                         .toList();

            weeklyStats.add(WeeklyState.from(date, userCommits));
        }

        return WeeklyCommitInfo.from(weeklyStats);
    }
}