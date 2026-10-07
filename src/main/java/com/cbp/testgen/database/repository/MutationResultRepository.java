package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.MutationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MutationResultRepository extends JpaRepository<MutationResultEntity, Long> {
    List<MutationResultEntity> findByClassMetadataIdOrderByTestRunRunTimestampAsc(Long classId);
    Optional<MutationResultEntity> findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(Long classId);
}
