package com.tcc.edlaine.controller;

import com.tcc.edlaine.domain.dto.FileJson;
import com.tcc.edlaine.domain.entities.DocumentEntity;
import com.tcc.edlaine.domain.entities.FileVersion;
import com.tcc.edlaine.domain.entities.SharedRecord;
import com.tcc.edlaine.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/upload")
    public ResponseEntity<FileJson> upload(@RequestParam("file") MultipartFile file,
                                           @RequestParam String type) {
        return fileService.uploadFile(file, type);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/share/{documentId}")
    public ResponseEntity<FileJson> shareFileByEmail(@PathVariable String documentId,
                                                     @RequestParam String email) {
        return fileService.shareFile(documentId, email); //todo recurso pago
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/upload/{documentId}")
    public ResponseEntity<FileJson> updateDocumentById(@RequestParam("file") MultipartFile file,
                                                       @PathVariable String documentId) {
        return fileService.updateFile(file, documentId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/share-history/{documentId}")
    public ResponseEntity<List<SharedRecord>> getShareHistory(@PathVariable String documentId) {
        return fileService.getShareHistory(documentId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/my-files")
    public ResponseEntity<List<DocumentEntity>> getMyFiles() {
        return fileService.getUserDocuments();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentEntity> getMyFilesById(@PathVariable String documentId) {
        return fileService.getDocumentById(documentId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all-files")
    public ResponseEntity<List<DocumentEntity>> getFiles() {
        return fileService.getAllDocuments();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        return fileService.downloadFileById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/file-versions/{documentId}")
    public ResponseEntity<List<FileVersion>> getFileVersions(@PathVariable String documentId) {
        log.info("Buscando documento com id :::: {}", documentId);
        return fileService.getFileVersionsById(documentId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/download-version/{documentId}/{versionIndex}")
    public ResponseEntity<Resource> downloadFileVersion(@PathVariable String documentId, @PathVariable int versionIndex) {
        return fileService.downloadFileVersionByVersionIndex(documentId, versionIndex);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{documentId}")
    public ResponseEntity<FileJson> deleteFile(@PathVariable String documentId) {
        return fileService.deleteDocumentById(documentId);
    }

}
