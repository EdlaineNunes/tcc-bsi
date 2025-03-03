package com.tcc.edlaine.crosscutting.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void testConnection() {
        try {
            // Listar todos os bancos de dados
            mongoTemplate.getDb().listCollectionNames().forEach(databaseName -> {
                System.out.println("Banco de dados encontrado: " + databaseName);
            });
        } catch (Exception e) {
            System.err.println("Erro ao tentar conectar ao MongoDB: " + e.getMessage());
        }
    }
}
