package com.tcc.edlaine.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcc.edlaine.domain.enums.DocumentType;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private DocumentType type;
    private List<FileVersion> versions = new ArrayList<>();

    private List<SharedRecord> shareHistory = new ArrayList<>();

    public DocumentEntity(String filename,
                          String customerEmail,
                          String fileId,
                          String type) {
        this.filename = filename;
        this.customerEmail = customerEmail;
        this.createdAt = LocalDateTime.now();
        this.type = DocumentType.fromString(type);

        addVersion(fileId, filename, LocalDateTime.now());
    }

    public DocumentEntity(String filename,
                          String customerEmail,
                          String fileId,
                          String documentId,
                          String type) {
        this.id = documentId;
        this.filename = filename;
        this.customerEmail = customerEmail;
        this.createdAt = LocalDateTime.now();
        this.type = DocumentType.fromString(type);

        addVersion(fileId, filename, LocalDateTime.now());
    }

    public void addVersion(String fileId, String filename, LocalDateTime dateTime) {
        versions.add(new FileVersion(fileId, filename, dateTime));
    }

    public FileVersion getLatestVersion() {
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    public FileVersion getVersionByIndex(int index) {
        if (index >= 0 && index < versions.size()) {
            return versions.get(index);
        }
        return null;
    }

    public void shareWithEmail(String email, String sharedBy) {
        String emailId = UUID.randomUUID().toString();
        SharedRecord sharedRecord = new SharedRecord(email, emailId, PermissionLevel.GUEST, LocalDateTime.now(), sharedBy);
        shareHistory.add(sharedRecord);
    }


}

