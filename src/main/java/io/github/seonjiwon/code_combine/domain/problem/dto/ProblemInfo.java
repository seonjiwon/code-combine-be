package io.github.seonjiwon.code_combine.domain.problem.dto;

import io.github.seonjiwon.code_combine.domain.problem.entity.ProblemTier;
import lombok.Builder;
import lombok.Getter;

@Builder
public record ProblemInfo (
    int problemNumber,
    String title,
    String language,
    ProblemTier tier
) {

}
