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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final AuthService authService;
    private final EmailService emailService;

    public ResponseEntity<FileJson> uploadFile(MultipartFile file) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);

            DocumentEntity document = saveDocument(file, user);
            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
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
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateGuestAccess(user);

            DocumentEntity document = updateDocument(file, user, documentId);
            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
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

            InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(resource.getInputStream().readAllBytes()));


            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(inputStreamResource);
        } catch (HttpClientErrorException e) {
            log.error("Client error while retrieving file: {}", e.getMessage());
            throw new FileUnprocessableEntity("Failed to process file request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving file: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving the file: " + e.getMessage());
        }
    }

    public ResponseEntity<List<FileVersion>> getFileVersionsById(String documentId) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            DocumentEntity document = findDocumentById(documentId);
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
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFilename() + "\"")
                    .body(resource);
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

            fileStorageService.deleteFile(documentId);

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
        log.info("Id no fileStrage ::::: {}", fileId);
        DocumentEntity document = new DocumentEntity(file.getOriginalFilename(), user.getEmail(), fileId, documentId);

        documentRepository.save(document);
        log.info("Document saved: {}", document.getId());
        return document;
    }

}
