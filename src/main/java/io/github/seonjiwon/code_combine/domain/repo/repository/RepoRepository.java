package io.github.seonjiwon.code_combine.domain.repo.repository;

import io.github.seonjiwon.code_combine.domain.repo.entity.Repo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoRepository extends JpaRepository<Repo, Long> {

    Optional<Repo> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
