package com.tcc.edlaine.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@Document(collection = "documents")
public class DocumentEntity {
    @Id
    private String id;
    private String filename;
    private String userId; // Quem fez o upload
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private List<FileVersion> versions = new ArrayList<>();
    private Map<String, PermissionLevel> sharedWith = new HashMap<>(); // E-mail do usuário -> Nível de permissão

    private List<SharedRecord> shareHistory = new ArrayList<>();

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
        FileVersion version = getLatestVersion();
        shareHistory.add(new SharedRecord(email, sharedBy, version, LocalDateTime.now()));
        sharedWith.put(email, PermissionLevel.GUEST);
    }

    // Método para verificar se o usuário tem permissão para acessar
    public boolean hasPermission(String email, PermissionLevel requiredPermission) {
        return sharedWith.containsKey(email) && sharedWith.get(email).ordinal() >= requiredPermission.ordinal();
    }


}

