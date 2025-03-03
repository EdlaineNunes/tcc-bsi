package com.tcc.edlaine.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class FileStorageService {

    private final GridFSBucket gridFSBucket;
    private final GridFsOperations gridFsOperations;

    public FileStorageService(GridFSBucket gridFSBucket, GridFsOperations gridFsOperations) {
        this.gridFSBucket = gridFSBucket;
        this.gridFsOperations = gridFsOperations;
    }

    // 🟢 SALVAR O ARQUIVO NO MONGO
    public String saveFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            ObjectId fileId = gridFSBucket.uploadFromStream(file.getOriginalFilename(), inputStream);
            return fileId.toHexString(); // Retorna o ID do arquivo salvo
        }
    }

    // 🔵 RECUPERAR O ARQUIVO DO MONGO
    public GridFsResource getFile(String fileId) {
        GridFSFile gridFSFile = gridFsOperations.findOne(query(where("_id").is(fileId)));

        if (gridFSFile == null) {
            return null;
        }

        return gridFsOperations.getResource(gridFSFile);
    }

    // 🔴 DELETAR UM ARQUIVO
    public void deleteFile(String fileId) {
        gridFSBucket.delete(new ObjectId(fileId));
    }
}