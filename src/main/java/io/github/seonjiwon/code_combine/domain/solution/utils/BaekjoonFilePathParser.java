package io.github.seonjiwon.code_combine.domain.solution.utils;

import io.github.seonjiwon.code_combine.domain.problem.entity.ProblemTier;
import io.github.seonjiwon.code_combine.domain.solution.code.SolutionErrorCode;
import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import io.github.seonjiwon.code_combine.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaekjoonFilePathParser {

    /**
     * 파일 경로에서 문제 정보 추출
     */
    public ProblemInfo parse(String filePath, ProblemTier tier) {
        validateFilePath(filePath);

        try {
            String problemDir = extractProblemDirectory(filePath);
            int problemNumber = extractProblemNumber(problemDir);
            String title = extractTitle(problemDir);
            String language = extractLanguage(filePath);

            log.debug("파일 경로 파싱 성공: 문제 번호={}, 제목={}, 언어={}", problemNumber, title, language);

            return ProblemInfo.builder()
                .problemNumber(problemNumber)
                .title(title)
                .language(language)
                .tier(tier)
                .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("파일 경로 파싱 실패: {}", filePath, e);
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }
    }

    /**
     * 파일 경로 유효성 검증
     */
    private void validateFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }

        // 최소한 "/" 두 개는 있어야 함 (디렉토리/문제디렉토리/파일)
        int slashCount = filePath.length() - filePath.replace("/", "").length();
        if (slashCount < 2) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }
    }

    /**
     * 문제 디렉토리 추출
     * 예: 백준/11286.절댓값 힙/Main.java -> 11286.절댓값 힙
     */
    private String extractProblemDirectory(String filePath) {
        // 1. 마지막 슬래시 찾기
        int lastSlash = filePath.lastIndexOf('/');
        // 2. 마지막 -1 슬래시 찾기
        int secondLastSlash = filePath.lastIndexOf('/', lastSlash - 1);

        if (secondLastSlash == -1 || lastSlash == -1) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }

        return filePath.substring(secondLastSlash + 1, lastSlash);
    }

    /**
     * 문제 번호 추출
     * 예: 11286.절댓값 힙 -> 11286
     */
    private int extractProblemNumber(String problemDir) {
        int dotIndex = problemDir.indexOf('.');

        if (dotIndex == -1) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }

        String numberStr = problemDir.substring(0, dotIndex).trim();
        return Integer.parseInt(numberStr);
    }

    /**
     * 문제 제목 추출
     * 예: 11286.절댓값 힙 -> 절댓값 힙
     */
    private String extractTitle(String problemDir) {
        int dotIndex = problemDir.indexOf('.');

        if (dotIndex == -1) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }

        return problemDir.substring(dotIndex + 1).strip();
    }

    /**
     * 언어 추출 (파일 확장자)
     * 예: Main.java -> java
     */
    private String extractLanguage(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        String codeName = filePath.substring(lastSlash);

        int dotIndex = codeName.lastIndexOf('.');

        if (dotIndex == -1) {
            throw new CustomException(SolutionErrorCode.INVALID_FILE_PATH);
        }

        return codeName.substring(dotIndex + 1);
    }
}
