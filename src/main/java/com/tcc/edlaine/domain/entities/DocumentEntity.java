package com.tcc.edlaine.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "documents")
public class DocumentEntity {
    @Id
    private String id;
    private String filename;
    private String customerEmail;
    private String fileStorageId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private List<FileVersion> versions = new ArrayList<>();

    private List<SharedRecord> shareHistory = new ArrayList<>();

    public DocumentEntity(String filename,
                          String customerEmail,
                          String fileId) {
        this.filename = filename;
        this.customerEmail = customerEmail;
        this.createdAt = LocalDateTime.now();

        addVersion(fileId, LocalDateTime.now());
    }

    public DocumentEntity(String filename,
                          String customerEmail,
                          String fileId,
                          String documentId) {
        this.id = documentId;
        this.filename = filename;
        this.customerEmail = customerEmail;
        this.createdAt = LocalDateTime.now();

        addVersion(fileId, LocalDateTime.now());
    }

    public void addVersion(String fileId, LocalDateTime dateTime) {
        versions.add(new FileVersion(fileId, dateTime));
    }

    // Recuperar a versão mais recente
    public FileVersion getLatestVersion() {
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    // Recuperar uma versão específica pelo índice
    public FileVersion getVersionByIndex(int index) {
        if (index >= 0 && index < versions.size()) {
            return versions.get(index);
        }
        return null;
    }

    // Método para compartilhar com um e-mail e definir permissão
    public void shareWithEmail(String email, String sharedBy) {
        String emailId = UUID.randomUUID().toString();
        SharedRecord sharedRecord = new SharedRecord(email, emailId, PermissionLevel.GUEST, LocalDateTime.now(), sharedBy);
        shareHistory.add(sharedRecord);
    }

    // Método para verificar se o usuário tem permissão para acessar
    public boolean hasPermission(String emailId, PermissionLevel requiredPermission) {
        for (SharedRecord record : shareHistory) {
            if (record.getEmailId().equals(emailId) && record.getPermissionLevel().ordinal() >= requiredPermission.ordinal()) {
                return true;
            }
        }
        return false;
    }


}

