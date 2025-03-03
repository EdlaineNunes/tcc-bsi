package com.tcc.edlaine.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SharedRecord {
    private String email;           // E-mail do destinatário
    private String sharedBy;        // Quem fez o envio do documento
    private FileVersion version;    // Versão do documento compartilhada
    private LocalDateTime sharedAt; // Data e hora do compartilhamento

}
