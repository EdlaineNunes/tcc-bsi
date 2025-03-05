package com.tcc.edlaine.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionLevel {
    GUEST(0, "Usuário convidado, não possui privilégios."),
    USER(1, "Usuário comum. Possui privilégios comuns."),
    ADMIN(2, "Usuário adminstrador. Possui privilégios de visualização de documentos vinculados a outros users."),
    SUPER_ADMIN(3, "Usuário com privilégio de deleção, super adminstrador do sistema.");

    private final int level;
    private final String description;
}
