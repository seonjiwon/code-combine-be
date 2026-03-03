package io.github.seonjiwon.code_combine.domain.repo.repository;

import io.github.seonjiwon.code_combine.domain.repo.domain.Repo;
import io.github.seonjiwon.code_combine.domain.repo.domain.SyncStatus;
import io.github.seonjiwon.code_combine.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepoRepository extends JpaRepository<Repo, Long> {

    Optional<Repo> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    List<Repo> findBySyncStatusAndRetryCountLessThan(SyncStatus status, int retryCount);
}
