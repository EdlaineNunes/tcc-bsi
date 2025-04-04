package com.tcc.edlaine.repository;

import com.tcc.edlaine.domain.entities.DocumentEntity;
import com.tcc.edlaine.domain.enums.DocumentType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByCustomerEmail(String customerEmail);
    Optional<DocumentEntity> findByVersionsFileId(String fileId);
    List<DocumentEntity> findByType(DocumentType type);
}
