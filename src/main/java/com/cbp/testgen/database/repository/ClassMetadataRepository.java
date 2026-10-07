package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.ClassMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassMetadataRepository extends JpaRepository<ClassMetadataEntity, Long> {
    List<ClassMetadataEntity> findByProjectId(Long projectId);
    Optional<ClassMetadataEntity> findByProjectIdAndClassName(Long projectId, String className);
}
