package io.github.seonjiwon.code_combine.global.infra.github;

import java.time.ZonedDateTime;

public interface GitHubApiClient {

    String getUserRepos(String token);

    String getCommits(String token, String owner, String repo, ZonedDateTime since, ZonedDateTime until);

    String getCommits(String token, String owner, String repo, int page, int perPage);

    String getCommitDetail(String token, String owner, String repo, String sha);

    String getFileContent(String token, String owner, String repo, String path, String ref);
}