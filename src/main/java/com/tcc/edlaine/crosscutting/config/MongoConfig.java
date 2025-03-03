package com.tcc.edlaine.crosscutting.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    // Injeção do MongoTemplate
    @Autowired
    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Criação do GridFSBucket
    @Bean
    public GridFSBucket gridFSBucket() {
        MongoDatabase database = mongoTemplate.getDb();  // Obtém o banco de dados configurado no MongoTemplate
        return GridFSBuckets.create(database);
    }
}
