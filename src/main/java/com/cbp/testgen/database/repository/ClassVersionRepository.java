package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.ClassVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassVersionRepository extends JpaRepository<ClassVersionEntity, Long> {
    List<ClassVersionEntity> findByClassMetadataIdOrderByVersionNumberDesc(Long classId);
    Optional<ClassVersionEntity> findTopByClassMetadataIdOrderByVersionNumberDesc(Long classId);
}
