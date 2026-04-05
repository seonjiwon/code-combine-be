package io.github.seonjiwon.code_combine.domain.sync.service;

import io.github.seonjiwon.code_combine.domain.problem.dto.ProblemInfo;
import io.github.seonjiwon.code_combine.domain.solution.service.SolutionSyncService;
import io.github.seonjiwon.code_combine.domain.solution.utils.BaekjoonFilePathParser;
import io.github.seonjiwon.code_combine.domain.user.entity.User;
import io.github.seonjiwon.code_combine.global.infra.github.GitHubFetcher;
import io.github.seonjiwon.code_combine.global.infra.github.dto.GitHubCommitDetail;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SingleCommitSynchronizer {

    private final SolutionSyncService solutionSyncService;
    private final GitHubFetcher fetcher;
    private final BaekjoonFilePathParser filePathParser;

    /**
     * 한 개의 커밋 동기화
     */
    public void syncSingleCommit(User user, String token, String owner, String repo,
                                  String commitSha) {
        // 1. 중복 체크
        if (solutionSyncService.existsByCommitSha(commitSha)) {
            log.debug("이미 동기화된 커밋: {}", commitSha);
            return;
        }

        // 2. 커밋 상세 정보 조회
        GitHubCommitDetail commitDetail = fetcher.fetchCommitDetail(token, owner, repo, commitSha);

        // 3. 소스 코드 파일 찾기
        String sourceCodePath = findSourceCodePath(commitDetail.filePaths());
        if (sourceCodePath == null) {
            log.debug("소스 코드 파일 없음: commitSha={}", commitSha);
            return;
        }

        // 4. 파일 경로에서 문제 정보 파싱
        ProblemInfo problemInfo = filePathParser.parse(sourceCodePath);

        // 5. 소스 코드 가져오기
        String sourceCode = fetcher.fetchFileContent(token, owner, repo, sourceCodePath, commitSha);

        // 6. DB 저장
        solutionSyncService.saveSolution(user, problemInfo, sourceCode,
            commitSha, sourceCodePath, commitDetail);
    }

    /**
     * 소스 코드 파일 경로 찾기. README.md를 제외한 첫 번째 파일
     */
    private String findSourceCodePath(List<String> filePaths) {
        return filePaths.stream()
                        .filter(path -> !path.endsWith("README.md"))
                        .findFirst()
                        .orElse(null);
    }
}
