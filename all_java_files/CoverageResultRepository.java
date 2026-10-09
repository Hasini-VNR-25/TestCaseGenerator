package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.CoverageResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoverageResultRepository extends JpaRepository<CoverageResultEntity, Long> {
    List<CoverageResultEntity> findByClassMetadataIdOrderByTestRunRunTimestampAsc(Long classId);
    Optional<CoverageResultEntity> findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(Long classId);
}
