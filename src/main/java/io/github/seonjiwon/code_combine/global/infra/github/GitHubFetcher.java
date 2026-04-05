package io.github.seonjiwon.code_combine.global.infra.github;

import io.github.seonjiwon.code_combine.domain.repo.dto.GitHubRepoResponse;
import io.github.seonjiwon.code_combine.global.infra.github.dto.GitHubCommitDetail;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubFetcher {

    private final GitHubApiClient apiClient;
    private final GitHubResponseParser parser;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MAX_PER_PAGE = 100;

    /**
     * Repository 목록 불러오기
     */
    public List<String> fetchUserRepos(String token) {
        log.debug("사용자 Repository 조회");
        String response = apiClient.getUserRepos(token);
        return parser.parseRepos(response);
    }

    /**
     * 오늘 날짜의 커밋 SHA 목록 조회
     */
    public List<String> fetchTodayCommitShas(String token, String owner, String repo) {
        LocalDate today = LocalDate.now(KST);

        ZonedDateTime since = today.atStartOfDay(KST).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime until = today.plusDays(1).atStartOfDay(KST).withZoneSameInstant(ZoneOffset.UTC);

        log.debug("오늘 커밋 조회: {}/{}, 기간: {} ~ {}", owner, repo, since, until);

        String response = apiClient.getCommits(token, owner, repo, since, until);
        List<String> commitShas = parser.parseCommitShas(response);

        log.debug("오늘 커밋 {} 개 발견", commitShas.size());
        return commitShas;
    }

    /**
     * 모든 커밋 SHA 목록 조회
     */
    public List<String> fetchAllCommitShas(String token, String owner, String repo) {
        List<String> allCommitShas = new ArrayList<>();
        int page = 1;

        while (true) {
            // 1. 커밋 가져오기 MaxPage = 100
            String response = apiClient.getCommits(token, owner, repo, page, MAX_PER_PAGE);
            List<String> pageShas = parser.parseCommitShas(response);

            if (pageShas.isEmpty()) {
                break;
            }

            allCommitShas.addAll(pageShas);

            // 마지막 페이지 체크
            if (pageShas.size() < MAX_PER_PAGE) {
                break;
            }

            page++;
        }

        // 오래된 커밋부터 처리
        Collections.reverse(allCommitShas);

        log.debug("전체 커밋 조회 완료: 총 {} 개", allCommitShas.size());
        return allCommitShas;
    }

    /**
     * 커밋 상세 정보 조회
     */
    public GitHubCommitDetail fetchCommitDetail(String token, String owner, String repo, String sha) {
        String response = apiClient.getCommitDetail(token, owner, repo, sha);
        GitHubCommitDetail detail = parser.parseCommitDetail(response);

        return detail;
    }

    /**
     * 파일 내용 조회
     */
    public String fetchFileContent(String token, String owner, String repo, String path, String ref) {
        log.debug("파일 내용 조회: path={}, ref={}", path, ref);
        return apiClient.getFileContent(token, owner, repo, path, ref);
    }
}
