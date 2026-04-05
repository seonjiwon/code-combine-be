package io.github.seonjiwon.code_combine.domain.solution.repository;

import io.github.seonjiwon.code_combine.domain.solution.entity.Solution;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolutionRepository extends JpaRepository<Solution, Long> {

    Boolean existsByCommitSha(String commitSha);


    @Query("SELECT s FROM Solution s " +
        "JOIN FETCH s.user " +
        "JOIN FETCH s.problem " +
        "WHERE s.problem.id IN :problemIds " +
        "ORDER BY s.problem.problemNumber")
    List<Solution> findAllByProblemIdsWithUser(@Param("problemIds") List<Long> problemIds);


    @Query("SELECT s FROM Solution s " +
        "JOIN FETCH s.user " +
        "JOIN FETCH s.problem " +
        "WHERE s.problem.id = :problemId")
    List<Solution> findAllByProblemIdWithUser(@Param("problemId") Long problemId);


    @Query("SELECT s FROM Solution s " +
        "JOIN FETCH s.user " +
        "JOIN FETCH s.problem " +
        "WHERE s.solvedAt >= :start AND s.solvedAt < :end " +
        "ORDER BY s.solvedAt")
    List<Solution> findAllByPeriodWithUser(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);


}