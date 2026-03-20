package com.documentreader;

import com.documentreader.config.DocumentReaderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DocumentReaderProperties.class)
public class DocumentReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentReaderApplication.class, args);
    }
}
