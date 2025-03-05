package com.tcc.edlaine.domain.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEntity {
    @Id
    private String id;
    private String username;
    private String cpf;
    private String email;
    private String password;
    private PermissionLevel permissionLevel;
    private boolean active;
}