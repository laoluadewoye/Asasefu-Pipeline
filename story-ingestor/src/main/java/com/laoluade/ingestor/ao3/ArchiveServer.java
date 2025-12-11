package com.laoluade.ingestor.ao3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class ArchiveServer {
    public static void main(String[] args) {
        SpringApplication.run(ArchiveServer.class, args);
    }
}
