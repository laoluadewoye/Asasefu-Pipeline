package com.laoluade.ingestor.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ExampleServer {
    @Autowired
    private ExampleService exampleService;

    @GetMapping("/example/start")
    public ExampleNumber startAsyncService() throws InterruptedException {
        return exampleService.startAsyncService();
    }

    @GetMapping("/example/get")
    public ExampleNumber getExampleNumber() {
        return exampleService.getExample();
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleServer.class, args);
    }
}
