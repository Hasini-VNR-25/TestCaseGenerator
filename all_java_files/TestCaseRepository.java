package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {
    List<TestCaseEntity> findByClassMetadataId(Long classId);
    List<TestCaseEntity> findByMethodMetadataId(Long methodId);
    List<TestCaseEntity> findByClassMetadataIdAndIsWeakTrue(Long classId);
}
