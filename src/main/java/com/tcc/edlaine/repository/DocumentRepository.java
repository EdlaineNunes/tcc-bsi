package com.tcc.edlaine.repository;

import com.tcc.edlaine.domain.entities.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    // Busca todos os documentos de um usuário específico
    List<DocumentEntity> findByCustomerEmail(String customerEmail);

    // Busca um documento pelo nome (caso precise)
    Optional<DocumentEntity> findByFilename(String filename);
}
