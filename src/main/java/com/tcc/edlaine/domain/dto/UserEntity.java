package com.tcc.edlaine.domain.dto;

import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    private String username;
    private String cpf;
    private String email;
    private String senha;
    private PermissionLevel permissionLevel;
    private boolean active;
}