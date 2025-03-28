package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.file.FileNotFound;
import com.tcc.edlaine.crosscutting.exceptions.file.FileUnprocessableEntity;
import com.tcc.edlaine.domain.dto.FileJson;
import com.tcc.edlaine.domain.entities.DocumentEntity;
import com.tcc.edlaine.domain.entities.FileVersion;
import com.tcc.edlaine.domain.entities.SharedRecord;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final AuthService authService;
    private final EmailService emailService;
    private static final List<String> VALID_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf", "xls", "xlsx", "doc", "docx", "csv");


    public ResponseEntity<FileJson> uploadFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isEmpty()) {
                throw new FileUnprocessableEntity("File must have a valid extension. It appears the file is missing an extension.");
            }

            String extension = getFileExtension(fileName);

            if (extension.isEmpty() || !VALID_EXTENSIONS.contains(extension)) {
                throw new FileUnprocessableEntity("Invalid file extension. Allowed extensions are: " + VALID_EXTENSIONS);
            }

            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);

            DocumentEntity document = saveDocument(file, user);
            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (FileUnprocessableEntity e) {
            log.error("Invalid file extension: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to upload file. Details: " + e.getMessage());
        } catch (Exception e){
            log.error("Unexpected error while uploading file: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while uploading the file. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<FileJson> updateFile(MultipartFile file, String documentId) {
        try {
            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isEmpty()) {
                throw new FileUnprocessableEntity("File must have a valid extension. It appears the file is missing an extension.");
            }

            String extension = getFileExtension(fileName);

            if (extension.isEmpty() || !VALID_EXTENSIONS.contains(extension)) {
                throw new FileUnprocessableEntity("Invalid file extension. Allowed extensions are: " + VALID_EXTENSIONS);
            }

            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);

            DocumentEntity document = updateDocument(file, user, documentId);
            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (FileUnprocessableEntity e) {
            log.error("Invalid file extension: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to upload file. Details: " + e.getMessage());
        } catch (Exception e){
            log.error("Unexpected error while uploading file: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while uploading the file. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<FileJson> shareFile(String documentId, String email) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);

            DocumentEntity document = findDocumentById(documentId);
            document.shareWithEmail(email, user.getEmail());
            documentRepository.save(document);
            log.info("Initializing email sending for document {} and email {}", documentId, email);
            emailService.sendEmail(email, document.getFilename());

            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (HttpClientErrorException e) {
            log.error("Failed to share file: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to share file. Details: " + e.getMessage());
        } catch (Exception e){
            log.error("Unexpected error while sharing file: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while sharing the file. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<List<SharedRecord>> getShareHistory(String documentId) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            DocumentEntity document = findDocumentById(documentId);
            AuthService.validateUserAccess(user, document.getCustomerEmail());

            return ResponseEntity.ok(document.getShareHistory());
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve share history: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to retrieve share history. Details: " + e.getMessage());
        } catch (Exception e){
            log.error("Unexpected error while retrieving share history: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving share history. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<List<DocumentEntity>> getUserDocuments() {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);
            List<DocumentEntity> userFiles = documentRepository.findByCustomerEmail(user.getEmail());

            return ResponseEntity.ok(userFiles);
        } catch (Exception e){
            log.error("Failed to retrieve user documents: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to retrieve user documents. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<DocumentEntity> getDocumentById(String documentId) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);
            DocumentEntity document = findDocumentById(documentId);

            return ResponseEntity.ok(document);
        } catch (Exception e){
            log.error("Failed to retrieve document for id: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to retrieve documents for id. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<List<DocumentEntity>> getAllDocuments() {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            List<DocumentEntity> userFiles = documentRepository.findAll();

            return ResponseEntity.ok(userFiles);
        } catch (Exception e){
            log.error("Failed to retrieve all documents: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to retrieve all documents. Details: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadFileById(String id) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            DocumentEntity document = findDocumentById(id);
            AuthService.validateUserAccess(user, document.getCustomerEmail());

            GridFsResource resource = fileStorageService.getFile(document.getLatestVersion().getFileId());

            if (resource == null) {
                throw new FileNotFound("The requested file was not found in the storage.");
            }

            String fileName = resource.getFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            String mimeType = getMimeType(extension);

            InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")  // Garante o nome correto
                    .body(inputStreamResource);
        } catch (HttpClientErrorException e) {
            log.error("Client error while retrieving file: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to process file request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving file: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving the file: " + e.getMessage());
        }
    }


    public ResponseEntity<List<FileVersion>> getFileVersionsById(String versionId) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            Optional<DocumentEntity> optionalDocument = documentRepository.findByVersionsFileId(versionId);

            if (optionalDocument.isEmpty()) {
                throw new FileNotFound("Document with versionId " + versionId + " not found.");
            }

            DocumentEntity document = optionalDocument.get();
            AuthService.validateUserAccess(user, document.getCustomerEmail());

            return ResponseEntity.ok(document.getVersions());
        } catch (HttpClientErrorException e) {
            log.error("Client error while retrieving file versions: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to retrieve file versions: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving file versions: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving file versions: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadFileVersionByVersionIndex(String documentId, int versionIndex) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            DocumentEntity document = findDocumentById(documentId);
            AuthService.validateUserAccess(user, document.getCustomerEmail());

            FileVersion version = document.getVersionByIndex(versionIndex);
            if (version == null) {
                throw new FileNotFound("The requested document version was not found.");
            }

            GridFsResource resource = fileStorageService.getFile(version.getFileId());
            String fileName = resource.getFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            String mimeType = getMimeType(extension);

            InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")  // Garante o nome correto
                    .body(inputStreamResource);
        } catch (HttpClientErrorException e) {
            log.error("Client error while retrieving file version: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to process file version request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving file version: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving the file version: " + e.getMessage());
        }
    }

    public ResponseEntity<FileJson> deleteDocumentById(String documentId) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            DocumentEntity document = findDocumentById(documentId);
            AuthService.validadeUserAndAuthoritySuperAdmin(user);

            document.getVersions().forEach(fileVersion -> {
                fileStorageService.deleteFile(fileVersion.getFileId());
            });
            documentRepository.delete(document);

            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (HttpClientErrorException e) {
            log.error("Client error while deleting document: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to delete document: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while deleting document: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while deleting the document: " + e.getMessage());
        }
    }

    private DocumentEntity findDocumentById(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new FileNotFound("Document notFound"));
    }

    private DocumentEntity saveDocument(MultipartFile file, UserEntity user) throws IOException {
        String fileId = fileStorageService.saveFile(file);
        log.info("Id no fileStrage ::::: {}", fileId);
        DocumentEntity document = new DocumentEntity(file.getOriginalFilename(), user.getEmail(), fileId);

        documentRepository.save(document);
        log.info("Document saved: {}", document.getId());
        return document;
    }

    private DocumentEntity updateDocument(MultipartFile file, UserEntity user, String documentId) throws IOException {
        String fileId = fileStorageService.saveFile(file);
        log.info("Id no fileStorage ::::: {}", fileId);

        DocumentEntity document = findDocumentById(documentId);

        DocumentEntity updatedDocument = new DocumentEntity(
                file.getOriginalFilename(),
                user.getEmail(),
                fileId,
                document.getId()
        );

        List<FileVersion> allVersions = new ArrayList<>(document.getVersions());
        allVersions.add(new FileVersion(fileId, file.getOriginalFilename(), LocalDateTime.now()));
        updatedDocument.setVersions(allVersions);

        documentRepository.save(updatedDocument);
        log.info("Document updated: {}", updatedDocument.getId());

        return updatedDocument;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // Expressão regular para garantir que a extensão seja válida
        String regex = ".*\\.([a-zA-Z0-9]{2,4})$";  // A extensão deve ter entre 2 e 4 caracteres alfanuméricos

        // Verificando se o nome do arquivo corresponde ao padrão
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            String extension = matcher.group(1).toLowerCase();

            // Verificar se a extensão é válida
            if (VALID_EXTENSIONS.contains(extension)) {
                return extension;
            } else {
                throw new FileUnprocessableEntity("Invalid file extension. Allowed extensions are: " + VALID_EXTENSIONS);
            }
        }

        // Se não houver correspondência, retorna uma string vazia
        return "";
    }


    private String getMimeType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls" -> "application/vnd.ms-excel";
            case "csv" -> "text/csv";
            case "txt" -> "text/plain";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream"; // Default type if no match
        };
    }

}
