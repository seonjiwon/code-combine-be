package io.github.seonjiwon.code_combine.domain.sync.repository;

import io.github.seonjiwon.code_combine.domain.sync.entity.FailedCommit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedCommitRepository extends JpaRepository<FailedCommit, Long> {

    List<FailedCommit> findByRetryCountLessThan(int maxRetry);
}