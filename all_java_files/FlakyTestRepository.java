package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.FlakyTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlakyTestRepository extends JpaRepository<FlakyTestEntity, Long> {
    List<FlakyTestEntity> findByTestRunId(Long runId);

    @Query("SELECT f FROM FlakyTestEntity f WHERE f.testCase.classMetadata.id = :classId")
    List<FlakyTestEntity> findByClassId(@Param("classId") Long classId);
}
