package com.tcc.edlaine.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileVersion {
    private String fileId; // ID do arquivo salvo no GridFS
    private LocalDateTime uploadedAt;
}
