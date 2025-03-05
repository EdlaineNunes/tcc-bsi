package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.file.FileNotFound;
import com.tcc.edlaine.crosscutting.exceptions.file.FileUnprocessableEntity;
import com.tcc.edlaine.crosscutting.exceptions.user.UserAccessDenied;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.domain.dto.FileJson;
import com.tcc.edlaine.domain.entities.DocumentEntity;
import com.tcc.edlaine.domain.entities.FileVersion;
import com.tcc.edlaine.domain.entities.SharedRecord;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import com.tcc.edlaine.repository.DocumentRepository;
import com.tcc.edlaine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FileService {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public FileService(FileStorageService fileStorageService,
                          DocumentRepository documentRepository,
                          UserRepository userRepository,
                          EmailService emailService ) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public ResponseEntity<FileJson> uploadFile(MultipartFile file) {
        try {
            UserEntity user = getUserEntity();
            validadeUserAndAuthorityGuest(user);

            DocumentEntity document = saveDocumentEntity(file, user);
            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error saving file. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error with saving file ::: " + e.getMessage());
        }
    }

    public ResponseEntity<FileJson> shareFile(String documentId, String email) {
        try {
            UserEntity user = getUserEntity();
            validadeUserAndAuthorityGuest(user);

            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new FileNotFound("Document notFound"));


            document.shareWithEmail(email, user.getEmail());
            documentRepository.save(document);

            emailService.sendEmail(email, document.getFilename());

            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error share file. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error with share file ::: " + e.getMessage());
        }
    }

    public ResponseEntity<List<SharedRecord>> getShareHistoryDocument(String documentId) {
        try {
            UserEntity user = getUserEntity();

            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new FileNotFound("Document notFound"));

            validadeUserAndAuthorityUser(user, document.getCustomerEmail());

            return ResponseEntity.ok(document.getShareHistory());
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<List<DocumentEntity>> getMyDocuments() {
        try {
            UserEntity user = getUserEntity();
            validadeUserAndAuthorityGuest(user);
            List<DocumentEntity> userFiles = documentRepository.findByCustomerEmail(user.getEmail());

            return ResponseEntity.ok(userFiles);
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<List<DocumentEntity>> getAllDocuments() {
        try {
            UserEntity user = getUserEntity();
            validadeUserAndAuthorityAdmin(user);
            List<DocumentEntity> userFiles = documentRepository.findAll();

            return ResponseEntity.ok(userFiles);
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadFileById(String id) {
        try {
            UserEntity user = getUserEntity();
            DocumentEntity document = documentRepository.findById(id).orElseThrow(() -> new FileNotFound("Document notFound"));
            validadeUserAndAuthorityUser(user, document.getCustomerEmail());

            GridFsResource resource = fileStorageService.getFile(id);

            if (resource == null) {
                throw new FileNotFound("File not found");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<List<FileVersion>> getFileVersionsById(String documentId) {
        try {
            UserEntity user = getUserEntity();
            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new FileNotFound("Document notFound"));

            validadeUserAndAuthorityUser(user, document.getCustomerEmail());

            return ResponseEntity.ok(document.getVersions());
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadFileVersionByVersionIndex(String documentId, int versionIndex) {
        try {
            UserEntity user = getUserEntity();
            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new FileNotFound("Document notFound"));

            validadeUserAndAuthorityUser(user, document.getCustomerEmail());

            FileVersion version = document.getVersionByIndex(versionIndex);
            if (version == null) {
                throw new FileNotFound("Document notFound");
            }

            GridFsResource resource = fileStorageService.getFile(version.getFileId());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFilename() + "\"")
                    .body(resource);
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }

    public ResponseEntity<FileJson> deleteDocumentById(String documentId) {
        try {
            UserEntity user = getUserEntity();
            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new FileNotFound("Document notFound"));

            validadeUserAndAuthoritySuperAdmin(user);

            fileStorageService.deleteFile(documentId);

            return ResponseEntity.ok(new FileJson(document.getId(), document.getFilename()));
        } catch (HttpClientErrorException e) {
            log.error("Exception :::: {}", e.getMessage());
            throw new FileUnprocessableEntity("Error get share history. Message ::: " + e.getMessage());
        } catch (Exception e){
            log.error("Exception :::: {}", e.getMessage());
            throw new RuntimeException("Unexpected error to get share history ::: " + e.getMessage());
        }
    }


    private DocumentEntity saveDocumentEntity(MultipartFile file, UserEntity user) throws IOException {
        String fileId = fileStorageService.saveFile(file);
        DocumentEntity document = new DocumentEntity(file.getOriginalFilename(), user.getEmail(), fileId);

        documentRepository.save(document);
        return document;
    }

    public UserEntity getUserEntity() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFound("userEmail {" + userEmail + "} notFound"));
    }

    private static void validadeUserAndAuthorityGuest(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel() == PermissionLevel.GUEST) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    private static void validadeUserAndAuthorityUser(UserEntity user, String customerEmailDocument) {
        if (!user.isActive()
                || user.getPermissionLevel() == PermissionLevel.GUEST
                || !Objects.equals(user.getEmail(), customerEmailDocument)) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    private static void validadeUserAndAuthorityAdmin(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel() == PermissionLevel.GUEST
                || user.getPermissionLevel() == PermissionLevel.USER) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    private static void validadeUserAndAuthoritySuperAdmin(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel() == PermissionLevel.GUEST
                || user.getPermissionLevel() == PermissionLevel.USER
                || user.getPermissionLevel() == PermissionLevel.ADMIN) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }


}
