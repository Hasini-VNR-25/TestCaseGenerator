package com.cbp.testgen.database.repository;

import com.cbp.testgen.database.entity.MethodMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodMetadataRepository extends JpaRepository<MethodMetadataEntity, Long> {
    List<MethodMetadataEntity> findByClassMetadataId(Long classId);
}
