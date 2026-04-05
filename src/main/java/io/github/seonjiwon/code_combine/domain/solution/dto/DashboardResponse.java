package io.github.seonjiwon.code_combine.domain.solution.dto;

import io.github.seonjiwon.code_combine.domain.problem.entity.ProblemTier;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

public class DashboardResponse {

    @Builder
    public record WeeklyCommitInfo(
        List<WeeklyState> weeklyStats
    ) {

        public static WeeklyCommitInfo from(List<WeeklyState> weeklyStats) {
            return WeeklyCommitInfo.builder()
                                   .weeklyStats(weeklyStats)
                                   .build();
        }
    }

    @Builder
    public record WeeklyState(
        LocalDate date,
        Integer dailyTotalUser,
        List<UserCommit> userCommits
    ) {

        public static WeeklyState from(LocalDate date, List<UserCommit> userCommits) {
            return WeeklyState.builder()
                              .date(date)
                              .dailyTotalUser(userCommits.size())
                              .userCommits(userCommits)
                              .build();
        }
    }

    @Builder
    public record UserCommit(
        Long userId,
        String username,
        String avatarUrl,
        Integer commitCount,
        List<SolvedProblemInfo> solvedProblems
    ) {}

    @Builder
    public record SolvedProblemInfo(
        Long problemId,
        String problemName,
        ProblemTier tier
    ) {}
}