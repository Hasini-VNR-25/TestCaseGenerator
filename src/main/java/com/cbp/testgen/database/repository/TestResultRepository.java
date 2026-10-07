package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.TestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    List<TestResultEntity> findByTestRunId(Long runId);
    List<TestResultEntity> findByTestCaseId(Long testCaseId);
}
