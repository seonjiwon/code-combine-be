package io.github.seonjiwon.code_combine.domain.solution.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import io.github.seonjiwon.code_combine.domain.problem.entity.ProblemTier;
import io.github.seonjiwon.code_combine.domain.solution.code.SolutionErrorCode;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BaekjoonFilePathParserTest {

    private final BaekjoonFilePathParser parser = new BaekjoonFilePathParser();

    @ParameterizedTest
    @CsvSource({
        "백준/Gold/1000.A+B/Main.java, 1000, A+B, java",
        "백준/Gold/1001.C+D/solution.py, 1001, C+D, py",
        "백준/Gold/1002.123/Main.cpp, 1002, 123, cpp",
    })
    @DisplayName("정상경로 정상 파싱")
    void successfulParsingWithNormalPath(String filePath, int problemNumber, String title,
                                         String expectedLanguage) throws Exception {
        // given
        ProblemTier tier = ProblemTier.GOLD_V;
        // when
        ProblemInfo result = parser.parse(filePath, tier);

        // then
        assertThat(result.problemNumber()).isEqualTo(problemNumber);
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.language()).isEqualTo(expectedLanguage);
        assertThat(result.tier()).isEqualTo(ProblemTier.GOLD_V);
    }

    @Test
    @DisplayName("Tier가 Null인 경우")
    void successfulParsingWithNormalPathAndTierIsNull() throws Exception {
        // given
        String filePath = "백준/Gold/10026.적록색약/적록색약.java";
        ProblemTier tier = null;
        // when
        ProblemInfo result = parser.parse(filePath, tier);

        // then
        assertThat(result.problemNumber()).isEqualTo(10026);
        assertThat(result.title()).isEqualTo("적록색약");
        assertThat(result.language()).isEqualTo("java");
        assertThat(result.tier()).isEqualTo(null);
    }
    
    @Test
    @DisplayName("제목에 점이 포함된 경우 첫 번째 점 기준으로 분리한다.")
    void titleContainsDot() throws Exception{
        // given
        String filePath = "백준/Gold/10026.적록.색약/적록색약.java";
        
        // when
        ProblemInfo result = parser.parse(filePath, null);
    
        // then
        assertThat(result.problemNumber()).isEqualTo(10026);
        assertThat(result.title()).isEqualTo("적록.색약");
    }

    @Test
    @DisplayName("제목에 공백이 있을 경우 무시한다.")
    void titleContainsSpace() throws Exception{
        // given
        String filePath = "백준/Gold/10026.적록색약  /적록색약.java";

        // when
        ProblemInfo result = parser.parse(filePath, null);

        // then
        assertThat(result.problemNumber()).isEqualTo(10026);
        assertThat(result.title()).isEqualTo("적록색약");
    }

    @Test
    @DisplayName("확장자가 없을 경우 에러를 던진다.")
    void titleWithoutExtendName() throws Exception{
        // given
        String filePath = "백준/Gold/10026.적록색약/적록색약";

        // when, then
        assertThatThrownBy(() -> parser.parse(filePath, null))
            .isInstanceOf(CustomException.class)
            .hasMessage(SolutionErrorCode.INVALID_FILE_PATH.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("널 혹은 빈값이 넘어올 경우 에러를 발생시킨다.")
    void throwExceptionInputIsNullOrEmpty(String filePath) throws Exception{
        // when, then
        assertThatThrownBy(() -> parser.parse(filePath, null))
            .isInstanceOf(CustomException.class)
            .hasMessage(SolutionErrorCode.INVALID_FILE_PATH.getMessage());
    }
}