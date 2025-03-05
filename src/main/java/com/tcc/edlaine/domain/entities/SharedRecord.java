package com.tcc.edlaine.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SharedRecord {
    private String recipientEmail;   // E-mail do destinatário
    private String sharedBy;        // Quem fez o envio do documento
    private FileVersion version;    // Versão do documento compartilhada
    private LocalDateTime sharedAt; // Data e hora do compartilhamento

}
