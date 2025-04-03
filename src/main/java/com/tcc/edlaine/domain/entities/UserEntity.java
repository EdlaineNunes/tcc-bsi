package com.tcc.edlaine.domain.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes({
        @CompoundIndex(name = "unique_email_idx", def = "{'email': 1}", unique = true)
})
public class UserEntity {
    @Id
    private String id;
    private String username;
    @Indexed(unique = true)
    private String cpf;
    @Indexed(unique = true)
    private String email;
    private String password;
    private PermissionLevel permissionLevel;
    private boolean active;

    public UserEntity(String username,
                      String cpf,
                      String email,
                      String password,
                      PermissionLevel permissionLevel,
                      boolean active) {
        this.username = username;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.permissionLevel = permissionLevel;
        this.active = active;
    }
}