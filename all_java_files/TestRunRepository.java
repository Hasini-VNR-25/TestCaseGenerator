package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.TestRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRunRepository extends JpaRepository<TestRunEntity, Long> {
    List<TestRunEntity> findByProjectIdOrderByRunTimestampDesc(Long projectId);
    Optional<TestRunEntity> findTopByProjectIdOrderByRunTimestampDesc(Long projectId);
}
