package com.tcc.edlaine.domain.entities;

import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SharedRecord {
    private String email; // E-mail do destinatário
    private String emailId; // ID único para o e-mail de compartilhamento
    private PermissionLevel permissionLevel; // Nível de permissão do usuário
    private LocalDateTime sharedAt; // Data e hora do compartilhamento
    private String sharedBy; // E-mail de quem fez o compartilhamento

}
