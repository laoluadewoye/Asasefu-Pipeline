package com.laoluade.ingestor.ao3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Top-level class that runs the Spring Boot backend using the @SpringBootApplication annotation.
 *
 * @author Laolu Ade
 */
@SpringBootApplication
public class ArchiveServer {
    public static void main(String[] args) {
        SpringApplication.run(ArchiveServer.class, args);
    }
}
