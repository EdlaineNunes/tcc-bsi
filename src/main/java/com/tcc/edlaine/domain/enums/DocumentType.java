package com.tcc.edlaine.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
    PUBLIC("Documentos vísiveis para todos os usuários, com exceção de Guest"),
    FINANCIAL("Documentos fiscais são restritos, e vísiveis apenas para os usuários COUNTER, ADMIN e SUPER_ADMIN");

    private final String description;

    public static DocumentType fromString(String type) {
        for (DocumentType docType : DocumentType.values()) {
            if (docType.name().equalsIgnoreCase(type)) {
                return docType;
            }
        }
        throw new IllegalArgumentException("Document type invalid: " + type);
    }
}
