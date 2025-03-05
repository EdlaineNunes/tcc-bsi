package com.tcc.edlaine.repository;

import com.tcc.edlaine.domain.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByCpf(String cpf);
}
