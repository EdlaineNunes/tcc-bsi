package com.tcc.edlaine.repository;

import org.springframework.boot.test.context.SpringBootTest;

//@Testcontainers
//@SpringBootTest
public class DocumentRepositoryTest {

//    @Container
//    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.10");
//
//    @DynamicPropertySource
//    static void setProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//    }
//
//    @Autowired
//    private DocumentRepository repository;
//
//    @Test
//    void testFindByUserId() {
//        DocumentEntity doc = new DocumentEntity();
//        doc.setUserId("123");
//        doc.setFilename("teste.pdf");
//        repository.save(doc);
//
//        List<DocumentEntity> results = repository.findByUserId("123");
//        assertFalse(results.isEmpty());
//    }
}
