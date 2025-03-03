package com.tcc.edlaine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.tcc.edlaine.repository")
public class TccEdlaineBsiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TccEdlaineBsiApplication.class, args);
	}

}
