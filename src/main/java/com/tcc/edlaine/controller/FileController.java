package com.tcc.edlaine.controller;

import com.tcc.edlaine.domain.dto.DocumentEntity;
import com.tcc.edlaine.domain.dto.FileVersion;
import com.tcc.edlaine.domain.dto.SharedRecord;
import com.tcc.edlaine.domain.dto.UserEntity;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import com.tcc.edlaine.repository.DocumentRepository;
import com.tcc.edlaine.repository.UserRepository;
import com.tcc.edlaine.service.EmailService;
import com.tcc.edlaine.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final EmailService emailService;

    public FileController(FileStorageService fileStorageService, UserRepository userRepository, DocumentRepository documentRepository, EmailService emailService) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.emailService = emailService;
    }

    // 🟢 FAZER UPLOAD DO ARQUIVO
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário inativo. Não é possível realizar upload.");
            }

            String fileId = fileStorageService.saveFile(file);

            DocumentEntity document = new DocumentEntity();
            document.setFilename(file.getOriginalFilename());
            document.setUserId(user.getEmail());
            document.addVersion(fileId, LocalDateTime.now());

            documentRepository.save(document);

            return ResponseEntity.ok("Arquivo salvo! ID do documento: " + document.getId());

        } catch (Exception e) {
            log.error("Exception :::: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo.");
        }
    }

    // Endpoint para compartilhar o arquivo com outro usuário por e-mail
    @PostMapping("/share/{documentId}")
    public ResponseEntity<String> shareFileByEmail(@PathVariable String documentId, @RequestParam String email) {
        try {
            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            // Verifica se o usuário tem permissão para compartilhar o documento
            if (user.getPermissionLevel() == PermissionLevel.GUEST) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para compartilhar este documento.");
            }

            // Compartilhar o arquivo com base no e-mail fornecido
            document.shareWithEmail(email, userEmail); // Método para adicionar um usuário à lista de compartilhados
            documentRepository.save(document);

            // Envia um e-mail para o usuário compartilhado
            String subject = "Compartilhamento de Documento";
            String messageText = "Você recebeu acesso ao documento: " + document.getFilename();
            emailService.sendEmail(email, subject, messageText);

            return ResponseEntity.ok("Documento compartilhado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao compartilhar o arquivo.");
        }
    }

    //visualizar historico de compartilhamento de um documento
    @GetMapping("/share-history/{documentId}")
    public ResponseEntity<List<SharedRecord>> getShareHistory(@PathVariable String documentId) {
        try {
            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            return ResponseEntity.ok(document.getShareHistory());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 📌 LISTA TODOS OS ARQUIVOS DO USUÁRIO LOGADO
    @GetMapping("/my-files")
    public ResponseEntity<List<DocumentEntity>> getMyFiles() {
        try {
            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            List<DocumentEntity> userFiles = documentRepository.findByUserId(user.getId());
            return ResponseEntity.ok(userFiles);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔵 FAZER DOWNLOAD DO ARQUIVO PELO ID - versao atualizada
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        try {
//            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
//            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();
//
//            DocumentEntity document = documentRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
//
//            // Usuários sem permissao cadastrada
//            if (user.getPermissionLevel() == PermissionLevel.UNKNOW) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//            }
            documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            GridFsResource resource = fileStorageService.getFile(id);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para buscar versões de um arquivo
    @GetMapping("/file-versions/{documentId}")
    public ResponseEntity<List<FileVersion>> getFileVersions(@PathVariable String documentId) {
        try {
            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            // Verifica se o usuário comum pode ver as versões do arquivo
            if (user.getPermissionLevel() == PermissionLevel.USER && !document.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            return ResponseEntity.ok(document.getVersions());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // Endpoint para baixar uma versão específica do arquivo
    @GetMapping("/download-version/{documentId}/{versionIndex}")
    public ResponseEntity<Resource> downloadFileVersion(@PathVariable String documentId, @PathVariable int versionIndex) {
        try {
            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            DocumentEntity document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            // Usuários comuns podem baixar versões apenas dos documentos que eles fizeram upload
            if (user.getPermissionLevel() == PermissionLevel.USER && !document.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FileVersion version = document.getVersionByIndex(versionIndex);
            if (version == null) {
                return ResponseEntity.notFound().build();
            }

            GridFsResource resource = fileStorageService.getFile(version.getFileId());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // 🔴 DELETAR UM ARQUIVO PELO ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable String id) {
        try {
            // Obtém o e-mail do usuário autenticado
            String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElseThrow();

            // Encontra o documento pelo ID
            DocumentEntity document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

            // Verifica se o usuário tem o nível de permissão "SUPER_ADMIN"
            if (user.getPermissionLevel() != PermissionLevel.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para excluir este arquivo.");
            }

            // Deleta o arquivo do sistema de armazenamento
            fileStorageService.deleteFile(id);

            // Responde com sucesso
            return ResponseEntity.ok("Arquivo removido com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao excluir o arquivo.");
        }
    }
}
